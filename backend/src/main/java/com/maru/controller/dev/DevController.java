package com.maru.controller.dev;

// TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.

import com.maru.common.util.CookieUtil;
import com.maru.common.util.JwtUtil;
import com.maru.controller.auth.dto.TokenRes;
import com.maru.controller.dev.dto.CreateTestUserReq;
import com.maru.domain.user.User;
import com.maru.repository.user.UserRepository;
import com.maru.service.dev.DevDojangSeeder;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Profile({"dev", "local"})
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class DevController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final DevDojangSeeder devDojangSeeder;

    @PostMapping("/create-test-user")
    @Transactional
    public ResponseEntity<Void> createTestUser(
        @Valid @RequestBody CreateTestUserReq request,
        HttpServletResponse response
    ) {
        User user = User.createWithoutRole(request.name(), null, request.phone());

        if (request.role() != null) {
            user.updateRole(request.role());
        }
        user.updateOnboardingStep(request.onboardingStep());

        User savedUser = userRepository.save(user);
        log.info("[DEV] 테스트 유저 생성: id={}, name={}, role={}, step={}",
            savedUser.getId(), savedUser.getName(), savedUser.getRole(), savedUser.getOnboardingStep());

        TokenRes tokenRes = generateTokenResponse(savedUser);
        cookieUtil.setAuthCookies(response, tokenRes);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/seed-dojangs")
    public ResponseEntity<SeedDojangsRes> seedDojangs() {
        log.info("[DEV] 도장 시드 데이터 생성 시작");
        int count = devDojangSeeder.seedDojangs();
        log.info("[DEV] 도장 시드 데이터 생성 완료: {}개", count);
        return ResponseEntity.ok(new SeedDojangsRes(count));
    }

    private TokenRes generateTokenResponse(User user) {
        String role = user.getRole() != null ? user.getRole().name() : "PENDING";

        String accessToken = jwtUtil.generateAccessToken(
            user.getId(),
            null,
            null,
            role
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return TokenRes.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .role(role)
            .build();
    }

    public record SeedDojangsRes(int count) {}
}
