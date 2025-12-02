package com.maru.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmploymentAwarePermissionEvaluator implements PermissionEvaluator {

    private final PermissionCache permissionCache;

    /**
     * 도메인 객체에 대한 권한 평가
     * JwtClaims에서 userId, tenantId, dojangId, role을 추출하여 권한 검증
     * - 관장(OWNER) 역할: 모든 권한 자동 부여
     * - 일반 역할: PermissionCache로 도장별 권한 검증
     *
     * @param authentication 인증 정보 (principal에 JwtClaims 포함)
     * @param targetDomainObject 대상 도메인 객체 (미사용)
     * @param permission 권한 (예: "STUDENT:READ")
     * @return 권한이 있으면 true
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof JwtClaims(Long userId, Long tenantId, Long dojangId, String role))) {
                log.warn("Principal이 JwtClaims 타입이 아닙니다: {}", principal.getClass());
                return false;
            }

            if (userId == null || tenantId == null || dojangId == null) {
                log.warn("JwtClaims에 필수 ID가 없습니다 - userId: {}, tenantId: {}, dojangId: {}",
                        userId, tenantId, dojangId);
                return false;
            }

            String permissionStr = permission.toString();
            String[] parts = permissionStr.split(":");
            if (parts.length != 2) {
                log.warn("잘못된 권한 형식: {}", permissionStr);
                return false;
            }

            String resource = parts[0];
            String action = parts[1];

            if ("OWNER".equals(role)) {
                log.debug("관장 역할 무제한 접근 - userId: {}, dojangId: {}, permission: {}",
                        userId, dojangId, permissionStr);
                return true;
            }

            boolean hasPermission = permissionCache.hasPermission(
                    userId, tenantId, dojangId, resource, action);

            if (!hasPermission) {
                log.warn("권한 거부 - userId: {}, dojangId: {}, permission: {}",
                        userId, dojangId, permissionStr);
            }

            return hasPermission;

        } catch (Exception e) {
            log.error("권한 평가 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 식별자와 타입을 통한 권한 평가
     * 첫 번째 hasPermission 메서드로 위임
     *
     * @param authentication 인증 정보
     * @param targetId 대상 객체 식별자 (미사용)
     * @param targetType 대상 객체 타입 (미사용)
     * @param permission 권한 (예: "STUDENT:READ")
     * @return 권한이 있으면 true
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, (Object) null, permission);
    }
}
