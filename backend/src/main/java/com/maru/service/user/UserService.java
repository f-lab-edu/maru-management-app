package com.maru.service.user;

import com.maru.common.exception.BusinessException;
import com.maru.domain.user.OnboardingStep;
import com.maru.domain.user.User;
import com.maru.domain.user.UserRole;
import com.maru.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 ID로 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     * @throws BusinessException USER_NOT_FOUND - 사용자가 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     */
    @Transactional(readOnly = true)
    public User getCurrentUser(Long userId) {
        return getUserById(userId);
    }

    /**
     * 온보딩 프로필 정보 업데이트
     *
     * @param userId 사용자 ID
     * @param name 이름
     * @param email 이메일
     * @param phone 전화번호
     * @return 업데이트된 사용자 엔티티
     * @throws BusinessException ONBOARDING_STAGE_INVALID - PROFILE_INPUT 단계가 아닐 경우
     */
    @Transactional
    public User updateOnboardingProfile(Long userId, String name, String email, String phone) {
        User user = getUserById(userId);
        validateOnboardingStep(user, OnboardingStep.PROFILE_INPUT);

        user.updateProfile(name, email, phone);
        user.updateOnboardingStep(OnboardingStep.ROLE_SELECT);

        log.info("온보딩 프로필 업데이트 완료: userId={}", userId);
        return user;
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
    public User updateOnboardingRole(Long userId, UserRole role) {
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
            throw new BusinessException(ONBOARDING_STAGE_INVALID);
        }
    }

    private void validateRole(UserRole role) {
        if (role == null) {
            throw new BusinessException(USER_INVALID_ROLE);
        }
    }

    private OnboardingStep determineNextStep(UserRole role) {
        return switch (role) {
            case OWNER -> OnboardingStep.DOJANG_INFO;
            case INSTRUCTOR -> OnboardingStep.APPROVAL_WAIT;
        };
    }
}