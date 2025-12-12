package com.maru.service.sms;

import java.time.Duration;
import java.util.Optional;

public interface VerificationCodeStore {

    /**
     * 인증번호 저장
     *
     * @param phone 전화번호
     * @param code 인증번호
     * @param userId 요청자 ID
     * @param ttl 유효 기간
     */
    void save(String phone, String code, Long userId, Duration ttl);

    /**
     * 요청자 ID 조회
     *
     * @param phone 전화번호
     * @return 요청자 ID (없으면 empty)
     */
    Optional<Long> getUserId(String phone);

    /**
     * 인증번호 상태 조회
     *
     * @param phone 전화번호
     * @return 인증번호 상태 (NOT_FOUND, EXPIRED, VALID)
     */
    VerificationCodeStatus getStatus(String phone);

    /**
     * 인증번호 조회 (만료 시 자동 삭제)
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
     * 재발송 제한 여부 확인
     *
     * @param phone 전화번호
     * @return 재발송 제한 중이면 true
     */
    boolean isResendLimited(String phone);

    /**
     * 인증 실패 횟수 증가
     *
     * @param phone 전화번호
     * @return 증가 후 실패 횟수
     */
    int incrementFailCount(String phone);

    /**
     * 인증 실패 횟수 조회
     *
     * @param phone 전화번호
     * @return 실패 횟수 (없으면 0)
     */
    int getFailCount(String phone);
}
