package com.maru.service.invoice;

import com.maru.domain.invoice.PgPaymentDetail;
import com.maru.repository.invoice.PgPaymentDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgPaymentCompensationHandler {

    private final PgPaymentDetailRepository pgPaymentDetailRepository;
    private final TossPaymentClient tossPaymentClient;

    /**
     * TX1 롤백 시 보상 취소 처리
     *
     * @param event 보상 취소 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCompensation(PgPaymentCompensationEvent event) {
        pgPaymentDetailRepository.findById(event.pgPaymentDetailId())
                .ifPresent(this::compensateCancel);
    }

    private void compensateCancel(PgPaymentDetail pendingDetail) {
        try {
            pendingDetail.markOrphaned();
            pgPaymentDetailRepository.save(pendingDetail);

            tossPaymentClient.cancel(pendingDetail.getPaymentKey(),
                    "시스템 오류로 인한 자동 취소", pendingDetail.getAmount(),
                    UUID.randomUUID().toString());

            pgPaymentDetailRepository.delete(pendingDetail);
            log.info("보상 취소 성공: paymentKey={}", pendingDetail.getPaymentKey());
        } catch (Exception e) {
            log.warn("보상 취소 실패: paymentKey={} - ORPHANED 상태 유지, 운영자 수동 확인 필요",
                    pendingDetail.getPaymentKey(), e);
        }
    }
}
