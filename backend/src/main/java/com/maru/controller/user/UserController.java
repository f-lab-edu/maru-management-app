package com.maru.controller.user;

import com.maru.controller.user.dto.*;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.user.OAuthProvider;
import com.maru.domain.user.User;
import com.maru.security.CurrentUserId;
import com.maru.service.employment.EmploymentQueryService;
import com.maru.service.tenant.TenantService;
import com.maru.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "사용자")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TenantService tenantService;
    private final EmploymentQueryService employmentQueryService;

    /**
     * 현재 로그인한 사용자 정보 조회
     *
     * @param userId 현재 인증된 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<UserMeRes> getCurrentUser(@CurrentUserId String userId) {
        User user = userService.getCurrentUser(userId);
        OAuthProvider provider = userService.getOAuthProvider(userId);

        return ResponseEntity.ok(UserMeRes.from(user, provider));
    }

    /**
     * 온보딩 프로필 정보 업데이트
     *
     * @param userId 현재 인증된 사용자 ID
     * @param request 프로필 정보 (name, email, phone)
     * @return 업데이트된 프로필 정보
     */
    @PostMapping("/onboarding/profile")
    public ResponseEntity<OnboardingProfileRes> updateOnboardingProfile(
            @CurrentUserId String userId,
            @Valid @RequestBody OnboardingProfileReq request) {
        User user = userService.updateOnboardingProfile(
                userId,
                request.name(),
                request.email(),
                request.phone()
        );

        return ResponseEntity.ok(OnboardingProfileRes.from(user));
    }

    /**
     * 온보딩 역할 선택
     *
     * @param userId 현재 인증된 사용자 ID
     * @param request 역할 정보 (role)
     * @return 업데이트된 역할 정보
     */
    @PostMapping("/onboarding/role")
    public ResponseEntity<OnboardingRoleRes> updateOnboardingRole(
            @CurrentUserId String userId,
            @Valid @RequestBody OnboardingRoleReq request) {
        User user = userService.updateOnboardingRole(userId, request.role());

        return ResponseEntity.ok(OnboardingRoleRes.from(user));
    }

    /**
     * 도장 개설 (테넌트 및 도장 생성)
     *
     * @param userId 현재 인증된 사용자 ID
     * @param request 도장 정보 (name, address, phone)
     * @return 생성된 도장 정보
     */
    @PostMapping("/onboarding/dojang")
    public ResponseEntity<OnboardingDojangRes> createDojang(
            @CurrentUserId String userId,
            @Valid @RequestBody OnboardingDojangReq request) {
        Dojang dojang = tenantService.createTenantWithDojang(
                userId,
                request.name(),
                request.address(),
                request.phone()
        );

        User user = userService.getCurrentUser(userId);

        return ResponseEntity.ok(OnboardingDojangRes.from(user, dojang));
    }

    /**
     * 온보딩 스텝 롤백 (이전 단계로 자동 이동)
     *
     * @param userId 현재 인증된 사용자 ID
     * @return 업데이트된 사용자 정보
     */
    @PostMapping("/onboarding/step/previous")
    public ResponseEntity<UserMeRes> rollbackOnboardingStep(@CurrentUserId String userId) {
        User user = userService.rollbackOnboardingStep(userId);
        OAuthProvider provider = userService.getOAuthProvider(userId);

        return ResponseEntity.ok(UserMeRes.from(user, provider));
    }

    /**
     * 소속 도장 목록 조회
     *
     * @param userId 현재 인증된 사용자 ID
     * @return 도장 목록 및 개수
     */
    @GetMapping("/me/dojangs")
    public ResponseEntity<MyDojangsRes> getMyDojangs(@CurrentUserId String userId) {
        List<MyDojangRes> dojangs = employmentQueryService.getMyDojangs(userId);
        return ResponseEntity.ok(MyDojangsRes.from(dojangs));
    }

    /**
     * 사용자 프로필 수정 (이름, 전화번호)
     *
     * @param userId 현재 인증된 사용자 ID
     * @param request 프로필 수정 정보
     * @return 수정된 사용자 정보
     */
    @PatchMapping("/me")
    public ResponseEntity<UserMeRes> updateMyProfile(
            @CurrentUserId String userId,
            @Valid @RequestBody UserProfileUpdateReq request) {
        User user = userService.updateMyProfile(userId, request.name(), request.phone());
        OAuthProvider provider = userService.getOAuthProvider(userId);

        return ResponseEntity.ok(UserMeRes.from(user, provider));
    }
}
