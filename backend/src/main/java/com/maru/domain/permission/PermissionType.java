package com.maru.domain.permission;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 사범 권한 타입 정의
 * JSON 컬럼에 저장되며, JPA Converter를 통해 Set으로 변환됨
 */
@Getter
@RequiredArgsConstructor
public enum PermissionType {

    // 원생 관리
    STUDENT_VIEW("student", "view", "원생 정보 조회", true),
    STUDENT_CREATE("student", "create", "원생 등록", true),
    STUDENT_UPDATE("student", "update", "원생 정보 수정", true),
    STUDENT_DELETE("student", "delete", "원생 삭제", false),

    // 출석 관리
    ATTENDANCE_VIEW("attendance", "view", "출석 조회", true),
    ATTENDANCE_UPDATE("attendance", "update", "출석 체크/수정", true),

    // 수납 관리
    PAYMENT_VIEW("payment", "view", "수납 조회", true),
    PAYMENT_UPDATE("payment", "update", "청구서/수납 관리", true),

    // 도장 관리
    DOJANG_UPDATE_INFO("dojang", "updateInfo", "도장 정보 수정", false),
    DOJANG_MANAGE_CLASS("dojang", "manageClass", "수련반 관리", true),

    // 통계
    STATS_VIEW_DASHBOARD("stats", "viewDashboard", "대시보드 조회", true);

    private final String resource;
    private final String action;
    private final String description;
    private final boolean defaultGranted;

    /**
     * resource:action 형태의 코드 반환
     */
    public String getCode() {
        return resource + ":" + action;
    }

    /**
     * resource:action 코드로 PermissionType 조회
     */
    public static PermissionType fromCode(String code) {
        return Arrays.stream(values())
                .filter(p -> p.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 권한 코드: " + code));
    }

    /**
     * resource와 action으로 PermissionType 조회
     */
    public static PermissionType of(String resource, String action) {
        return fromCode(resource + ":" + action);
    }

    /**
     * 기본 권한 목록 반환 (defaultGranted=true인 권한들)
     */
    public static Set<PermissionType> getDefaultPermissions() {
        return Arrays.stream(values())
                .filter(PermissionType::isDefaultGranted)
                .collect(Collectors.toSet());
    }

    /**
     * 모든 권한 목록 반환
     */
    public static Set<PermissionType> getAllPermissions() {
        return Arrays.stream(values())
                .collect(Collectors.toSet());
    }

    /**
     * 필수 권한 목록 반환 (해제 불가능한 권한)
     */
    public static Set<PermissionType> getRequiredPermissions() {
        return Set.of(STUDENT_VIEW);
    }
}
