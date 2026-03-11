package com.maru.service.invoice;

import com.maru.domain.invoice.PgPaymentDetail;
import com.maru.repository.invoice.PgPaymentDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PgPaymentPendingSaver {

    private final PgPaymentDetailRepository pgPaymentDetailRepository;

    /**
     * PENDING 상태의 PgPaymentDetail 저장 (독립 트랜잭션)
     *
     * @param tenantId 테넌트 ID
     * @param dojangId 도장 ID
     * @param paymentKey 토스 결제 키
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return 저장된 PgPaymentDetail
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PgPaymentDetail savePending(String tenantId, String dojangId,
                                        String paymentKey, String orderId, BigDecimal amount) {
        PgPaymentDetail detail = PgPaymentDetail.createPending(
                tenantId, dojangId, paymentKey, orderId, amount);
        return pgPaymentDetailRepository.save(detail);
    }

    /**
     * PENDING 상태의 PgPaymentDetail 삭제 (독립 트랜잭션)
     *
     * @param pendingDetail 삭제할 PgPaymentDetail
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deletePending(PgPaymentDetail pendingDetail) {
        pgPaymentDetailRepository.delete(pendingDetail);
    }
}
