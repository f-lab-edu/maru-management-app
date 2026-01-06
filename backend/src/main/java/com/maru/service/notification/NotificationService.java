package com.maru.service.notification;

import com.maru.common.exception.BusinessException;
import com.maru.domain.attendance.Attendance;
import com.maru.domain.attendance.exception.AttendanceErrorCode;
import com.maru.domain.guardian.Guardian;
import com.maru.domain.message.MessageQueue;
import com.maru.domain.student.Student;
import com.maru.domain.student.exception.StudentErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.attendance.AttendanceRepository;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.message.MessageQueueRepository;
import com.maru.repository.student.StudentRepository;
import com.maru.repository.tenant.DojangRepository;
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
    private final StudentRepository studentRepository;
    private final GuardianshipRepository guardianshipRepository;
    private final MessageQueueRepository messageQueueRepository;
    private final DojangRepository dojangRepository;

    /**
     * 출석(체크인) 알림 메시지 생성
     *
     * @param attendanceId 출석 기록 ID
     * @return 생성된 메시지 ID 목록
     */
    @Transactional
    public List<String> createCheckinNotification(String attendanceId) {
        Attendance attendance = findAttendance(attendanceId);
        Student student = findStudent(attendance.getStudentId());
        String dojangName = findDojangName(attendance.getDojangId());

        return createNotification(
                attendance,
                student,
                "출석 알림",
                "[" + dojangName + "] " + student.getName() + " 수련생이 출석했습니다."
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
        Attendance attendance = findAttendance(attendanceId);
        Student student = findStudent(attendance.getStudentId());
        String dojangName = findDojangName(attendance.getDojangId());

        return createNotification(
                attendance,
                student,
                "하원 알림",
                "[" + dojangName + "] " + student.getName() + " 수련생이 하원했습니다."
        );
    }

    private List<String> createNotification(Attendance attendance, Student student, String title, String body) {
        List<Guardian> guardians = findGuardians(student.getId());

        if (guardians.isEmpty()) {
            log.info("알림 대상 학부모 없음: attendanceId={}", attendance.getId());
            return List.of();
        }

        List<MessageQueue> messages = createMessages(attendance, student, guardians, title, body);
        messageQueueRepository.saveAll(messages);

        List<String> messageIds = messages.stream().map(MessageQueue::getId).toList();
        log.info("알림 생성 완료: attendanceId={}, title={}, messageCount={}",
                attendance.getId(), title, messageIds.size());

        return messageIds;
    }

    private Attendance findAttendance(String attendanceId) {
        return attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new BusinessException(AttendanceErrorCode.NOT_FOUND));
    }

    private Student findStudent(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.NOT_FOUND));
    }

    private String findDojangName(String dojangId) {
        return dojangRepository.findById(dojangId)
                .map(Dojang::getName)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));
    }

    private List<Guardian> findGuardians(String studentId) {
        return guardianshipRepository.findGuardiansByStudentId(studentId, true);
    }

    private List<Guardian> findGuardians(String studentId, boolean primaryOnly) {
        return guardianshipRepository.findGuardiansByStudentId(studentId, primaryOnly);
    }

    private List<MessageQueue> createMessages(Attendance attendance, Student student,
                                               List<Guardian> guardians, String title, String body) {
        List<MessageQueue> messages = new ArrayList<>();
        for (Guardian guardian : guardians) {
            MessageQueue message = MessageQueue.createAttendanceNotification(
                    attendance.getTenantId(),
                    attendance.getDojangId(),
                    guardian,
                    student,
                    title,
                    body
            );
            messages.add(message);
        }
        return messages;
    }
}
