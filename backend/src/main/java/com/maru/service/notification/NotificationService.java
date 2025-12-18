package com.maru.service.notification;

import com.maru.common.exception.BusinessException;
import com.maru.domain.attendance.Attendance;
import com.maru.domain.attendance.exception.AttendanceErrorCode;
import com.maru.domain.guardian.Guardian;
import com.maru.domain.message.MessageQueue;
import com.maru.repository.attendance.AttendanceRepository;
import com.maru.repository.guardian.GuardianshipRepository;
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
    public List<Long> createCheckinNotification(Long attendanceId) {
        Attendance attendance = findAttendance(attendanceId);
        String studentName = attendance.getStudent().getName();

        return createNotification(
                attendance,
                "출석 알림",
                studentName + " 수련생이 출석했습니다."
        );
    }

    /**
     * 하원(체크아웃) 알림 메시지 생성
     *
     * @param attendanceId 출석 기록 ID
     * @return 생성된 메시지 ID 목록
     */
    @Transactional
    public List<Long> createCheckoutNotification(Long attendanceId) {
        Attendance attendance = findAttendance(attendanceId);
        String studentName = attendance.getStudent().getName();

        return createNotification(
                attendance,
                "하원 알림",
                studentName + " 수련생이 하원했습니다."
        );
    }

    private List<Long> createNotification(Attendance attendance, String title, String body) {
        List<Guardian> guardians = findGuardians(attendance.getStudent().getId());

        if (guardians.isEmpty()) {
            log.info("알림 대상 학부모 없음: attendanceId={}", attendance.getId());
            return List.of();
        }

        List<MessageQueue> messages = createMessages(attendance, guardians, title, body);
        messageQueueRepository.saveAll(messages);

        List<Long> messageIds = messages.stream().map(MessageQueue::getId).toList();
        log.info("알림 생성 완료: attendanceId={}, title={}, messageCount={}",
                attendance.getId(), title, messageIds.size());

        return messageIds;
    }

    private Attendance findAttendance(Long attendanceId) {
        return attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new BusinessException(AttendanceErrorCode.NOT_FOUND));
    }

    private List<Guardian> findGuardians(Long studentId) {
        return guardianshipRepository.findGuardiansByStudentId(studentId, true);
    }

    private List<Guardian> findGuardians(Long studentId, boolean primaryOnly) {
        return guardianshipRepository.findGuardiansByStudentId(studentId, primaryOnly);
    }

    private List<MessageQueue> createMessages(Attendance attendance, List<Guardian> guardians,
                                               String title, String body) {
        List<MessageQueue> messages = new ArrayList<>();
        for (Guardian guardian : guardians) {
            MessageQueue message = MessageQueue.createAttendanceNotification(
                    attendance.getTenantId(),
                    attendance.getDojangId(),
                    guardian,
                    attendance.getStudent(),
                    title,
                    body
            );
            messages.add(message);
        }
        return messages;
    }
}
