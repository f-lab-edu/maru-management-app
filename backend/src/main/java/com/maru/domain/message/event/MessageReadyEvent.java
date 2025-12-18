package com.maru.domain.message.event;

/**
 * 메시지 큐 저장 완료 후 비동기 발송 트리거 이벤트
 *
 * @param messageId 발송할 메시지 ID
 * @param tenantId 테넌트 ID
 */
public record MessageReadyEvent(
        Long messageId,
        Long tenantId
) {
}
