package com.maru.service.sms;

import java.time.Duration;
import java.util.Optional;

public interface VerificationCodeStore {

    /**
     * 인증번호 저장
     *
     * @param phone 전화번호
     * @param code 인증번호
     * @param ttl 유효 기간
     */
    void save(String phone, String code, Duration ttl);

    /**
     * 인증번호 조회
     *
     * @param phone 전화번호
     * @return 인증번호 (만료되었거나 없으면 empty)
     */
    Optional<String> get(String phone);

    /**
     * 인증번호 삭제
     *
     * @param phone 전화번호
     */
    void delete(String phone);

    /**
     * 인증번호 존재 여부 확인 (재발송 제한용)
     *
     * @param phone 전화번호
     * @return 존재하면 true
     */
    boolean exists(String phone);
}
