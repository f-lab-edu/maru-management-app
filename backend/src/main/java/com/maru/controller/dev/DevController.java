package com.maru.controller.dev;

// TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.

import com.maru.common.exception.auth.AuthErrorCode;
import com.maru.common.exception.BusinessException;
import com.maru.common.util.CookieUtil;
import com.maru.common.util.JwtUtil;
import com.maru.controller.auth.dto.TokenRes;
import com.maru.controller.dev.dto.CreateTestUserReq;
import com.maru.domain.user.User;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.user.UserRepository;
import com.maru.security.JwtClaims;
import com.maru.service.dev.DevDojangSeeder;
import com.maru.service.dev.DevStudentSeeder;
import com.maru.service.search.dojang.DojangSearchIndexer;
import com.maru.service.search.dojang.DojangSearchService;
import com.maru.service.search.dojang.dto.DojangSearchDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Hidden
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
    private final DevStudentSeeder devStudentSeeder;
    private final DojangSearchService dojangSearchService;
    private final DojangSearchIndexer dojangSearchIndexer;
    private final DojangRepository dojangRepository;

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
    @Transactional
    public ResponseEntity<SeedDojangsRes> seedDojangs() {
        log.info("[DEV] 도장 시드 데이터 생성 시작");
        int count = devDojangSeeder.seedDojangs();
        dojangSearchService.refreshIndex();
        log.info("[DEV] 도장 시드 데이터 생성 완료: {}개", count);
        return ResponseEntity.ok(new SeedDojangsRes(count));
    }

    @PostMapping("/seed-students")
    public ResponseEntity<SeedStudentsRes> seedStudents() {
        JwtClaims claims = getCurrentJwtClaims();
        if (claims.tenantId() == null || claims.dojangId() == null) {
            throw new BusinessException(AuthErrorCode.ACCESS_DENIED);
        }

        log.info("[DEV] 테스트 학생 생성 시작 - tenantId: {}, dojangId: {}",
                claims.tenantId(), claims.dojangId());
        int count = devStudentSeeder.seedStudents(claims.tenantId(), claims.dojangId());
        log.info("[DEV] 테스트 학생 생성 완료: {}명", count);

        return ResponseEntity.ok(new SeedStudentsRes(count));
    }

    private JwtClaims getCurrentJwtClaims() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtClaims)) {
            throw new BusinessException(AuthErrorCode.ACCESS_DENIED);
        }
        return (JwtClaims) auth.getPrincipal();
    }

    private TokenRes generateTokenResponse(User user) {
        String role = user.getRole() != null ? user.getRole().name() : "PENDING";

        String accessToken = jwtUtil.generateAccessToken(
            user.getId(),
            null,
            null,
            role
        );
        String refreshToken = jwtUtil.generateRefreshToken(
            user.getId(),
            null,
            null,
            role
        );

        return TokenRes.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .tenantId(null)
            .dojangId(null)
            .role(role)
            .build();
    }

    public record SeedDojangsRes(int count) {}

    public record SeedStudentsRes(int count) {}

    @GetMapping("/search/inverted")
    public ResponseEntity<List<DojangSearchDto>> searchInverted(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(
                dojangSearchIndexer.search(keyword, PageRequest.of(0, size)).getContent()
        );
    }

    @GetMapping("/search/like")
    public ResponseEntity<List<DojangSearchDto>> searchLike(@RequestParam String keyword) {
        List<DojangSearchDto> results = dojangRepository.findByKeywordLike(keyword)
                .stream()
                .map(DojangSearchDto::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/fulltext")
    public ResponseEntity<List<DojangSearchDto>> searchFullText(@RequestParam String keyword) {
        List<DojangSearchDto> results = dojangRepository.findByKeywordFullText(keyword)
                .stream()
                .map(DojangSearchDto::from)
                .toList();
        return ResponseEntity.ok(results);
    }
}
