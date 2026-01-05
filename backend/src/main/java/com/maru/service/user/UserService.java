package com.maru.service.user;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.OnboardingErrorCode;
import com.maru.common.util.MaskingUtil;
import com.maru.domain.user.*;
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
public class UserService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final PhoneVerificationService phoneVerificationService;

    /**
     * 사용자 ID로 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     * @throws BusinessException USER_NOT_FOUND - 사용자가 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND));
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     */
    @Transactional(readOnly = true)
    public User getCurrentUser(String userId) {
        return getUserById(userId);
    }

    /**
     * 사용자의 OAuth 프로바이더 조회
     *
     * @param userId 사용자 ID
     * @return OAuth 프로바이더 (없으면 null)
     */
    @Transactional(readOnly = true)
    public OAuthProvider getOAuthProvider(String userId) {
        return oAuthAccountRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
            .map(OAuthAccount::getProvider)
            .orElse(null);
    }

    /**
     * 온보딩 프로필 정보 업데이트
     *
     * @param userId 사용자 ID
     * @param name 이름
     * @param email 이메일
     * @param phone 전화번호 (SMS 인증 완료된 번호와 일치해야 함)
     * @return 업데이트된 사용자 엔티티
     * @throws BusinessException ONBOARDING_STAGE_INVALID - PROFILE_INPUT 단계가 아닐 경우
     * @throws BusinessException ONBOARDING_PHONE_NOT_VERIFIED - 전화번호 인증 미완료
     */
    @Transactional
    public User updateOnboardingProfile(String userId, String name, String email, String phone) {
        User user = getUserById(userId);
        validateOnboardingStep(user, OnboardingStep.PROFILE_INPUT);
        validatePhoneVerified(user, phone);

        user.updateProfile(name, email, user.getPhone());
        user.updateOnboardingStep(OnboardingStep.ROLE_SELECT);

        log.info("온보딩 프로필 업데이트 완료: userId={}", userId);
        return user;
    }

    private void validatePhoneVerified(User user, String requestPhone) {
        if (user.getPhone() == null || !user.getPhone().equals(requestPhone)) {
            throw new BusinessException(OnboardingErrorCode.PHONE_NOT_VERIFIED);
        }
    }

    /**
     * 온보딩 역할 선택
     *
     * @param userId 사용자 ID
     * @param role 선택한 역할
     * @return 업데이트된 사용자 엔티티
     * @throws BusinessException ONBOARDING_STAGE_INVALID - ROLE_SELECT 단계가 아닐 경우
     * @throws BusinessException USER_INVALID_ROLE - 역할이 null일 경우
     */
    @Transactional
    public User updateOnboardingRole(String userId, UserRole role) {
        User user = getUserById(userId);
        validateOnboardingStep(user, OnboardingStep.ROLE_SELECT);
        validateRole(role);

        user.updateRole(role);
        user.updateOnboardingStep(determineNextStep(role));

        log.info("온보딩 역할 선택 완료: userId={}, role={}", userId, role);
        return user;
    }

    private void validateOnboardingStep(User user, OnboardingStep expectedStep) {
        if (user.getOnboardingStep() != expectedStep) {
            throw new BusinessException(OnboardingErrorCode.STAGE_INVALID);
        }
    }

    private void validateRole(UserRole role) {
        if (role == null) {
            throw new BusinessException(UserErrorCode.INVALID_ROLE);
        }
    }

    private OnboardingStep determineNextStep(UserRole role) {
        return switch (role) {
            case OWNER -> OnboardingStep.DOJANG_INFO;
            case INSTRUCTOR -> OnboardingStep.APPROVAL_WAIT;
        };
    }

    /**
     * 전화번호로 사용자 조회
     *
     * @param phone 전화번호
     * @return Optional<User>
     */
    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

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
            .map(existingUser -> mergeAndReturnResult(userId, existingUser))
            .orElseGet(() -> assignPhoneAndReturnResult(userId, phone));
    }

    private Optional<User> findExistingUserByPhone(String phone, String currentUserId) {
        return findByPhone(phone)
            .filter(user -> !user.getId().equals(currentUserId));
    }

    private PhoneVerificationRes mergeAndReturnResult(String currentUserId, User existingUser) {
        mergeOAuthAccount(currentUserId, existingUser.getId());
        log.info("계정 통합 완료: currentUserId={} → existingUserId={}", currentUserId, existingUser.getId());
        return new PhoneVerificationRes(existingUser.getId(), true);
    }

    private void mergeOAuthAccount(String currentUserId, String existingUserId) {
        moveOAuthAccountToExistingUser(currentUserId, existingUserId);
        deleteCurrentUser(currentUserId);
    }


    private void moveOAuthAccountToExistingUser(String currentUserId, String existingUserId) {
        OAuthAccount oAuthAccount = oAuthAccountRepository.findTopByUserIdOrderByCreatedAtDesc(currentUserId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND));
        User existingUser = getUserById(existingUserId);
        oAuthAccount.changeUser(existingUser);
        oAuthAccountRepository.flush();
        log.info("OAuth 계정 이동: userId {} → {}", currentUserId, existingUserId);
    }

    private void deleteCurrentUser(String userId) {
        userRepository.deleteById(userId);
        log.info("사용자 삭제: userId={}", userId);
    }

    private PhoneVerificationRes assignPhoneAndReturnResult(String userId, String phone) {
        User user = getUserById(userId);
        user.updateProfile(user.getName(), user.getEmail(), phone);
        log.info("전화번호 설정: userId={}, phone={}", userId, MaskingUtil.phone(phone));
        return new PhoneVerificationRes(userId, false);
    }

    /**
     * 온보딩 스텝 롤백 (이전 단계로 자동 이동)
     *
     * @param userId 사용자 ID
     * @return 업데이트된 사용자 엔티티
     * @throws BusinessException ONBOARDING_STAGE_INVALID - 롤백 불가능한 단계
     */
    @Transactional
    public User rollbackOnboardingStep(String userId) {
        User user = getUserById(userId);
        OnboardingStep currentStep = user.getOnboardingStep();
        OnboardingStep previousStep = getPreviousStep(currentStep);

        user.updateOnboardingStep(previousStep);
        log.info("온보딩 스텝 롤백: userId={}, {} → {}", userId, currentStep, previousStep);
        return user;
    }

    private OnboardingStep getPreviousStep(OnboardingStep currentStep) {
        return switch (currentStep) {
            case ROLE_SELECT -> OnboardingStep.PROFILE_INPUT;
            case DOJANG_INFO, APPROVAL_WAIT -> OnboardingStep.ROLE_SELECT;
            case PROFILE_INPUT, COMPLETED -> throw new BusinessException(OnboardingErrorCode.STAGE_INVALID);
        };
    }
}
