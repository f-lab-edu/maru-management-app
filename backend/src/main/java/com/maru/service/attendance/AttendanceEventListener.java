package com.maru.service.attendance;

import com.maru.domain.attendance.event.AttendanceCheckedEvent;
import com.maru.domain.message.event.MessageReadyEvent;
import com.maru.security.TenantContextHolder;
import com.maru.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AttendanceEventListener {

    private final NotificationService notificationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 출석/하원 체크 시 알림 생성 및 즉시 발송 트리거
     *
     * @param event 출석 체크 완료 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAttendanceChecked(AttendanceCheckedEvent event) {
        try (AutoCloseable ignored = TenantContextHolder.withTenant(event.tenantId())) {
            List<Long> messageIds = createNotificationMessages(event);
            triggerAsyncDispatch(messageIds, event.tenantId());
            logSuccess(event, messageIds.size());
        } catch (Exception e) {
            log.error("알림 생성 실패 (출석 기록 보존됨): attendanceId={}", event.attendanceId(), e);
        }
    }

    private List<Long> createNotificationMessages(AttendanceCheckedEvent event) {
        return event.isCheckin()
                ? notificationService.createCheckinNotification(event.attendanceId())
                : notificationService.createCheckoutNotification(event.attendanceId());
    }

    private void triggerAsyncDispatch(List<Long> messageIds, Long tenantId) {
        for (Long messageId : messageIds) {
            applicationEventPublisher.publishEvent(new MessageReadyEvent(messageId, tenantId));
        }
    }

    private void logSuccess(AttendanceCheckedEvent event, int messageCount) {
        log.info("출석 알림 생성 완료: attendanceId={}, isCheckin={}, messageCount={}",
                event.attendanceId(), event.isCheckin(), messageCount);
    }
}
