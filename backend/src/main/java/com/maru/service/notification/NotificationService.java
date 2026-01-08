package com.maru.service.notification;

import com.maru.common.exception.BusinessException;
import com.maru.domain.attendance.exception.AttendanceErrorCode;
import com.maru.domain.message.MessageQueue;
import com.maru.repository.attendance.AttendanceRepository;
import com.maru.repository.attendance.view.NotificationTargetView;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.guardian.view.GuardianshipView;
import com.maru.repository.message.MessageQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AttendanceRepository attendanceRepository;
    private final GuardianshipRepository guardianshipRepository;
    private final MessageQueueRepository messageQueueRepository;

    /**
     * 출석(체크인) 알림 메시지 생성
     *
     * @param attendanceId 출석 기록 ID
     * @return 생성된 메시지 ID 목록
     */
    @Transactional
    public List<String> createCheckinNotification(String attendanceId) {
        NotificationTargetView target = findNotificationTarget(attendanceId);
        return createNotification(
                target,
                "출석 알림",
                "[" + target.getDojangName() + "] " + target.getStudentName() + " 수련생이 출석했습니다."
        );
    }

    /**
     * 하원(체크아웃) 알림 메시지 생성
     *
     * @param attendanceId 출석 기록 ID
     * @return 생성된 메시지 ID 목록
     */
    @Transactional
    public List<String> createCheckoutNotification(String attendanceId) {
        NotificationTargetView target = findNotificationTarget(attendanceId);
        return createNotification(
                target,
                "하원 알림",
                "[" + target.getDojangName() + "] " + target.getStudentName() + " 수련생이 하원했습니다."
        );
    }

    private List<String> createNotification(NotificationTargetView target, String title, String body) {
        List<GuardianshipView> guardianships = guardianshipRepository
                .findPrimaryGuardianshipsByStudentId(target.getStudentId());

        if (guardianships.isEmpty()) {
            log.info("알림 대상 학부모 없음: attendanceId={}", target.getId());
            return List.of();
        }

        List<MessageQueue> messages = createMessages(target, guardianships, title, body);
        messageQueueRepository.saveAll(messages);

        List<String> messageIds = messages.stream().map(MessageQueue::getId).toList();
        log.info("알림 생성 완료: attendanceId={}, title={}, messageCount={}",
                target.getId(), title, messageIds.size());

        return messageIds;
    }

    private NotificationTargetView findNotificationTarget(String attendanceId) {
        return attendanceRepository.findNotificationTarget(attendanceId)
                .orElseThrow(() -> new BusinessException(AttendanceErrorCode.NOT_FOUND));
    }

    private List<MessageQueue> createMessages(NotificationTargetView target,
                                               List<GuardianshipView> guardianships, String title, String body) {
        List<MessageQueue> messages = new ArrayList<>();
        for (GuardianshipView guardianship : guardianships) {
            MessageQueue message = MessageQueue.createAttendanceNotification(
                    target.getTenantId(),
                    target.getDojangId(),
                    guardianship.getGuardianId(),
                    target.getStudentId(),
                    title,
                    body
            );
            messages.add(message);
        }
        return messages;
    }
}
