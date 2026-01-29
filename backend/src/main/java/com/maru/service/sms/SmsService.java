package com.maru.service.sms;

import java.util.List;
import java.util.Map;

public interface SmsService {

    /**
     * SMS 메시지 발송
     *
     * @param phone 수신자 전화번호
     * @param message 발송할 메시지
     */
    void send(String phone, String message);

    /**
     * SMS 메시지 배치 발송
     *
     * @param recipients 수신자 목록
     * @return dispatchId → vendorMessageId 매핑 (발송 성공한 것만)
     */
    Map<String, String> sendBatch(List<SmsRecipient> recipients);

    /**
     * SMS 제공자 이름 반환
     *
     * @return 제공자 이름
     */
    String getProviderName();

    /**
     * SMS 수신자 정보
     *
     * @param dispatchId 메시지 발송 ID (응답 매핑용)
     * @param phone 수신자 전화번호
     * @param message 발송할 메시지
     */
    record SmsRecipient(String dispatchId, String phone, String message) {}
}
