package com.maru.service.user;

import com.maru.common.exception.BusinessException;
import com.maru.common.util.MaskingUtil;
import com.maru.domain.user.OAuthAccount;
import com.maru.domain.user.User;
import com.maru.domain.user.exception.UserErrorCode;
import com.maru.repository.user.OAuthAccountRepository;
import com.maru.repository.user.UserRepository;
import com.maru.service.sms.PhoneVerificationService;
import com.maru.service.user.dto.PhoneVerificationRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMergeService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final PhoneVerificationService phoneVerificationService;

    /**
     * 인증번호 검증 및 계정 통합 처리
     *
     * @param userId 현재 사용자 ID
     * @param phone 전화번호
     * @param code 인증번호
     * @return 인증 결과 (userId, isExistingUser)
     */
    @Transactional
    public PhoneVerificationRes verifyPhoneAndMerge(String userId, String phone, String code) {
        phoneVerificationService.verifyCode(phone, code, userId);

        return findExistingUserByPhone(phone, userId)
            .map(existingUser -> mergeAccounts(userId, existingUser))
            .orElseGet(() -> assignPhone(userId, phone));
    }

    private Optional<User> findExistingUserByPhone(String phone, String currentUserId) {
        return userRepository.findByPhone(phone)
            .filter(user -> !user.getId().equals(currentUserId));
    }

    private PhoneVerificationRes mergeAccounts(String currentUserId, User existingUser) {
        moveOAuthAccount(currentUserId, existingUser);
        deleteUser(currentUserId);

        log.info("계정 통합 완료: currentUserId={} → existingUserId={}", currentUserId, existingUser.getId());
        return new PhoneVerificationRes(existingUser.getId(), true);
    }

    private void moveOAuthAccount(String fromUserId, User toUser) {
        OAuthAccount oAuthAccount = oAuthAccountRepository.findTopByUserIdOrderByCreatedAtDesc(fromUserId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND));

        oAuthAccount.changeUser(toUser);
        oAuthAccountRepository.flush();

        log.info("OAuth 계정 이동: userId {} → {}", fromUserId, toUser.getId());
    }

    private void deleteUser(String userId) {
        userRepository.deleteById(userId);
        log.info("사용자 삭제: userId={}", userId);
    }

    private PhoneVerificationRes assignPhone(String userId, String phone) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND));

        user.updateProfile(user.getName(), user.getEmail(), phone);

        log.info("전화번호 설정: userId={}, phone={}", userId, MaskingUtil.phone(phone));
        return new PhoneVerificationRes(userId, false);
    }
}
