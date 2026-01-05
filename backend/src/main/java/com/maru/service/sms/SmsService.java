package com.maru.service.sms;

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
     * @param recipients 수신자 목록 (전화번호, 메시지 쌍)
     */
    void sendBatch(java.util.List<SmsRecipient> recipients);

    /**
     * SMS 제공자 이름 반환
     *
     * @return 제공자 이름
     */
    String getProviderName();

    /**
     * SMS 수신자 정보
     */
    record SmsRecipient(String phone, String message) {}
}
