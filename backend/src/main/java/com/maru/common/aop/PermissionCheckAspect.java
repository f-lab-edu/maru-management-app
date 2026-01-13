package com.maru.common.aop;

import com.maru.domain.permission.PermissionType;
import com.maru.security.PermissionCache;
import com.maru.security.RequirePermission;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 권한 검증 AOP
 *
 * @see RequirePermission
 * @see AopOrder
 */
@Slf4j
@Aspect
@Component
@Order(AopOrder.PERMISSION_CHECK)
@RequiredArgsConstructor
public class PermissionCheckAspect {

    private final PermissionCache permissionCache;

    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        if (TenantContextHolder.isOwner() || TenantContextHolder.isSystemUser()) {
            log.debug("OWNER 또는 SYSTEM 사용자 - 권한 검증 스킵");
            return;
        }

        String userId = TenantContextHolder.getUserId();
        String tenantId = TenantContextHolder.getTenantId();
        String dojangId = TenantContextHolder.getDojangId();

        if (userId == null || tenantId == null || dojangId == null) {
            log.warn("권한 검증 실패 - 컨텍스트 정보 누락: userId={}, tenantId={}, dojangId={}",
                    userId, tenantId, dojangId);
            throw new AccessDeniedException("인증 정보가 없습니다");
        }

        PermissionType[] requiredPermissions = requirePermission.value();
        RequirePermission.LogicalOperator operator = requirePermission.operator();

        boolean hasPermission = checkPermissions(userId, tenantId, dojangId, requiredPermissions, operator);

        if (!hasPermission) {
            String permissionCodes = Arrays.stream(requiredPermissions)
                    .map(PermissionType::getCode)
                    .collect(Collectors.joining(", "));
            log.warn("권한 검증 실패: userId={}, dojangId={}, required=[{}], operator={}",
                    userId, dojangId, permissionCodes, operator);
            throw new AccessDeniedException("권한이 없습니다");
        }

        log.debug("권한 검증 성공: userId={}, dojangId={}", userId, dojangId);
    }

    private boolean checkPermissions(String userId, String tenantId, String dojangId,
            PermissionType[] permissions, RequirePermission.LogicalOperator operator) {
        if (operator == RequirePermission.LogicalOperator.AND) {
            return Arrays.stream(permissions).allMatch(p ->
                    permissionCache.hasPermission(userId, tenantId, dojangId, p.getResource(), p.getAction()));
        } else {
            return Arrays.stream(permissions).anyMatch(p ->
                    permissionCache.hasPermission(userId, tenantId, dojangId, p.getResource(), p.getAction()));
        }
    }
}
