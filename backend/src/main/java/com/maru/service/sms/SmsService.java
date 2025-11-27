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
     * SMS 제공자 이름 반환
     *
     * @return 제공자 이름
     */
    String getProviderName();
}
