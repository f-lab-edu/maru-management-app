package com.maru.service.invoice;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.config.properties.TossPaymentsProperties;
import com.maru.controller.invoice.dto.PaymentLinkRes;
import com.maru.controller.invoice.dto.PaymentPageRes;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.PaymentLink;
import com.maru.domain.invoice.exception.InvoiceErrorCode;
import com.maru.domain.invoice.exception.PgPaymentErrorCode;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.student.Student;
import com.maru.domain.tenant.Dojang;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.invoice.PaymentLinkRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.RequirePermission;
import com.maru.security.TenantContextHolder;
import com.maru.service.notification.MessageContentRenderer;
import com.maru.service.notification.MessageContentRenderer.RenderedContent;
import com.maru.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ValidateDojangAccess
public class PaymentLinkService {

    private static final String PAYMENT_PAGE_BASE_URL = "https://maru.app/pay/";
    private static final String ORDER_ID_PREFIX = "MARU-";

    private final PaymentLinkRepository paymentLinkRepository;
    private final InvoiceRepository invoiceRepository;
    private final StudentRepository studentRepository;
    private final DojangRepository dojangRepository;
    private final SmsService smsService;
    private final MessageContentRenderer contentRenderer;
    private final TossPaymentsProperties tossPaymentsProperties;

    /**
     * 결제 링크 생성 및 SMS 발송
     *
     * @param dojangId 도장 ID
     * @param invoiceId 청구서 ID
     * @return 생성된 결제 링크 정보
     * @throws BusinessException NOT_FOUND - 청구서를 찾을 수 없음
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public PaymentLinkRes createAndSend(String dojangId, String invoiceId) {
        String tenantId = TenantContextHolder.getTenantId();
        Invoice invoice = findInvoice(invoiceId, tenantId, dojangId);

        PaymentLink paymentLink = PaymentLink.create(tenantId, dojangId, invoiceId);
        paymentLinkRepository.save(paymentLink);

        sendSms(invoice, paymentLink);

        log.info("결제 링크 생성: invoiceId={}, token={}", invoiceId, paymentLink.getToken());
        return PaymentLinkRes.from(paymentLink);
    }

    /**
     * 결제 페이지 정보 조회 (인증 불필요)
     *
     * @param token 결제 링크 토큰
     * @return 결제 페이지에 표시할 정보
     * @throws BusinessException PAYMENT_LINK_NOT_FOUND - 토큰 없음
     * @throws BusinessException PAYMENT_LINK_EXPIRED - 만료됨
     * @throws BusinessException PAYMENT_LINK_ALREADY_USED - 이미 사용됨
     */
    @Transactional(readOnly = true)
    public PaymentPageRes getPaymentInfo(String token) {
        PaymentLink paymentLink = paymentLinkRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(PgPaymentErrorCode.PAYMENT_LINK_NOT_FOUND));

        paymentLink.validateUsable();

        Invoice invoice = findInvoice(
                paymentLink.getInvoiceId(), paymentLink.getTenantId(), paymentLink.getDojangId());

        Student student = studentRepository.findById(invoice.getStudentId())
                .orElseThrow(() -> new BusinessException(PgPaymentErrorCode.PAYMENT_LINK_NOT_FOUND));

        Dojang dojang = dojangRepository.findById(paymentLink.getDojangId())
                .orElseThrow(() -> new BusinessException(PgPaymentErrorCode.PAYMENT_LINK_NOT_FOUND));

        String orderId = generateOrderId(invoice.getId());

        return PaymentPageRes.builder()
                .invoiceId(invoice.getId())
                .studentName(student.getName())
                .billingYearMonth(invoice.getBillingYearMonth())
                .totalAmount(invoice.getAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .dojangName(dojang.getName())
                .clientKey(tossPaymentsProperties.clientKey())
                .orderId(orderId)
                .build();
    }

    private Invoice findInvoice(String invoiceId, String tenantId, String dojangId) {
        return invoiceRepository.findByIdAndTenantIdAndDojangId(invoiceId, tenantId, dojangId)
                .orElseThrow(() -> new BusinessException(InvoiceErrorCode.NOT_FOUND));
    }

    private void sendSms(Invoice invoice, PaymentLink paymentLink) {
        Student student = studentRepository.findById(invoice.getStudentId()).orElse(null);
        if (student == null || student.getPhone() == null) {
            log.warn("SMS 발송 실패: 원생 또는 전화번호 없음 - invoiceId={}", invoice.getId());
            return;
        }

        Dojang dojang = dojangRepository.findById(paymentLink.getDojangId()).orElse(null);
        String dojangName = dojang != null ? dojang.getName() : "";
        String paymentUrl = PAYMENT_PAGE_BASE_URL + paymentLink.getToken();

        RenderedContent content = contentRenderer.renderPaymentLink(
                dojangName, invoice.getBillingYearMonth(),
                invoice.getRemainingAmount(), paymentUrl);

        smsService.send(student.getPhone(), content.body());
    }

    private String generateOrderId(String invoiceId) {
        String tsid = UUID.randomUUID().toString().substring(0, 8);
        return ORDER_ID_PREFIX + invoiceId + "-" + tsid;
    }
}
