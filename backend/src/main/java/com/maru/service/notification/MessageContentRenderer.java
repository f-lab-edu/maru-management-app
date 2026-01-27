package com.maru.service.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maru.domain.message.MessageType;
import com.maru.repository.attendance.view.NotificationTargetView;
import com.maru.service.notification.exception.RenderingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageContentRenderer {

    private final ObjectMapper objectMapper;

    /**
     * 렌더링된 메시지 컨텐츠
     *
     * @param title 메시지 제목
     * @param body 메시지 본문
     * @param dataPayload 추가 데이터 (JSON 문자열, nullable)
     */
    public record RenderedContent(String title, String body, String dataPayload) {}

    /**
     * 출결 알림 메시지 렌더링
     *
     * @param messageType 메시지 유형 (ATTENDANCE_CHECKIN, ATTENDANCE_CHECKOUT)
     * @param target 알림 대상 정보
     * @return 렌더링된 컨텐츠
     * @throws RenderingException 지원하지 않는 메시지 타입인 경우
     */
    public RenderedContent renderAttendance(MessageType messageType, NotificationTargetView target) {
        return switch (messageType) {
            case ATTENDANCE_CHECKIN -> new RenderedContent(
                    "[" + target.getDojangName() + "] 출석 알림",
                    target.getStudentName() + " 수련생이 출석했습니다.",
                    serializeDataPayload(target)
            );
            case ATTENDANCE_CHECKOUT -> new RenderedContent(
                    "[" + target.getDojangName() + "] 하원 알림",
                    target.getStudentName() + " 수련생이 하원했습니다.",
                    serializeDataPayload(target)
            );
            default -> throw new RenderingException("지원하지 않는 메시지 타입: " + messageType);
        };
    }

    private String serializeDataPayload(NotificationTargetView target) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "studentId", target.getStudentId(),
                    "studentName", target.getStudentName(),
                    "dojangId", target.getDojangId(),
                    "dojangName", target.getDojangName(),
                    "attendanceId", target.getId()
            ));
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
