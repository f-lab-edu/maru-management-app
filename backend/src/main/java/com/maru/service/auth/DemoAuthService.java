package com.maru.service.auth;

import com.maru.common.exception.auth.AuthErrorCode;
import com.maru.common.exception.auth.AuthException;
import com.maru.controller.auth.dto.TokenRes;
import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.repository.employment.EmploymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemoAuthService {

    private final AuthService authService;
    private final EmploymentRepository employmentRepository;

    @Value("${maru.demo.user-id:}")
    private String demoUserId;

    /**
     * 데모 계정으로 로그인
     *
     * @return 발급된 토큰 정보
     */
    @Transactional(readOnly = true)
    public TokenRes login() {
        validateDemoConfigured();

        Employment employment = findFirstActiveEmployment();
        return authService.selectDojang(demoUserId, employment.getDojangId());
    }

    private void validateDemoConfigured() {
        if (demoUserId == null || demoUserId.isBlank()) {
            throw new AuthException(AuthErrorCode.DEMO_NOT_CONFIGURED);
        }
    }

    private Employment findFirstActiveEmployment() {
        return employmentRepository.findByUserIdAndStatus(demoUserId, EmploymentStatus.ACTIVE)
            .stream()
            .findFirst()
            .orElseThrow(() -> new AuthException(AuthErrorCode.NO_EMPLOYMENT));
    }
}
