package com.maru.service.employment;

import com.maru.common.exception.BusinessException;
import com.maru.controller.employment.dto.EmploymentRes;
import com.maru.controller.employment.dto.InstructorDetailRes;
import com.maru.controller.employment.dto.InstructorRes;
import com.maru.controller.employment.dto.PendingApprovalRes;
import com.maru.controller.user.dto.MyDojangRes;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.employment.exception.EmploymentErrorCode;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.user.UserRole;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.repository.employment.view.EmploymentDetailView;
import com.maru.repository.employment.view.InstructorView;
import com.maru.repository.employment.view.InstructorWithPermissionsView;
import com.maru.repository.employment.view.MyDojangView;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmploymentQueryService {

    private final EmploymentRepository employmentRepository;

    /**
     * 고용 정보 단건 조회
     *
     * @param employmentId 고용 ID
     * @return 고용 정보
     */
    public EmploymentRes getEmployment(String employmentId) {
        EmploymentDetailView view = employmentRepository.findViewById(employmentId)
                .orElseThrow(() -> new BusinessException(EmploymentErrorCode.NOT_FOUND));
        return toEmploymentRes(view);
    }

    /**
     * 도장의 대기 중인 승인 요청 목록 조회
     *
     * @param dojangId 도장 ID
     * @return 대기 중인 승인 요청 목록
     */
    public List<PendingApprovalRes> getPendingRequests(String dojangId) {
        return employmentRepository.findViewByDojangIdAndStatus(dojangId, EmploymentStatus.PENDING)
                .stream()
                .map(this::toPendingApprovalRes)
                .toList();
    }

    /**
     * 사용자의 승인 요청 목록 조회
     *
     * @param userId 사용자 ID
     * @return 승인 요청 목록
     */
    public List<EmploymentRes> getMyRequests(String userId) {
        return employmentRepository.findViewByUserId(userId)
                .stream()
                .map(this::toEmploymentRes)
                .toList();
    }

    /**
     * 사용자가 소속된 도장 목록 조회
     *
     * @param userId 사용자 ID
     * @return 소속 도장 목록
     */
    public List<MyDojangRes> getMyDojangs(String userId) {
        return employmentRepository.findMyDojangViewByUserIdAndStatus(userId, EmploymentStatus.ACTIVE)
                .stream()
                .map(this::toMyDojangRes)
                .toList();
    }

    /**
     * 사용자의 역할 결정 (관장/사범)
     *
     * @param userId 사용자 ID
     * @param dojangOwnerId 도장 관장 ID
     * @return 사용자 역할 (OWNER 또는 INSTRUCTOR)
     */
    public static UserRole resolveRole(String userId, String dojangOwnerId) {
        return dojangOwnerId.equals(userId) ? UserRole.OWNER : UserRole.INSTRUCTOR;
    }

    /**
     * 도장 소속 사범 목록 조회 (관장용)
     *
     * @param dojangId 도장 ID
     * @return 사범 목록
     */
    public List<InstructorRes> getInstructors(String dojangId) {
        String tenantId = TenantContextHolder.getTenantId();
        return employmentRepository.findInstructorsByDojangId(tenantId, dojangId)
                .stream()
                .map(this::toInstructorRes)
                .toList();
    }

    /**
     * 사범 상세 조회 (권한 포함)
     *
     * @param employmentId 고용 ID
     * @return 사범 상세 정보 (권한 포함)
     */
    public InstructorDetailRes getInstructorDetail(String employmentId) {
        String tenantId = TenantContextHolder.getTenantId();
        InstructorWithPermissionsView view = employmentRepository
                .findInstructorWithPermissionsById(tenantId, employmentId)
                .orElseThrow(() -> new BusinessException(EmploymentErrorCode.NOT_FOUND));
        return toInstructorDetailRes(view);
    }

    private EmploymentRes toEmploymentRes(EmploymentDetailView view) {
        return EmploymentRes.builder()
                .id(view.getId())
                .userId(view.getUserId())
                .dojangId(view.getDojangId())
                .dojangName(view.getDojangName())
                .status(view.getStatus())
                .requestedAt(view.getCreatedAt())
                .approvedAt(view.getStatus() == EmploymentStatus.ACTIVE ? view.getJoinedAt() : null)
                .rejectedAt(view.getStatus() == EmploymentStatus.REJECTED ? view.getEndedAt() : null)
                .build();
    }

    private PendingApprovalRes toPendingApprovalRes(EmploymentDetailView view) {
        return PendingApprovalRes.builder()
                .id(view.getId())
                .userId(view.getUserId())
                .userName(view.getUserName())
                .userEmail(view.getUserEmail())
                .userPhone(view.getUserPhone())
                .requestedAt(view.getCreatedAt())
                .status(view.getStatus())
                .build();
    }

    private MyDojangRes toMyDojangRes(MyDojangView view) {
        return MyDojangRes.builder()
                .dojangId(view.getDojangId())
                .dojangName(view.getDojangName())
                .tenantId(view.getTenantId())
                .role(resolveRole(view.getUserId(), view.getDojangOwnerId()))
                .build();
    }

    private InstructorRes toInstructorRes(InstructorView view) {
        return InstructorRes.builder()
                .id(view.getId())
                .userId(view.getUserId())
                .name(view.getUserName())
                .email(view.getUserEmail())
                .phone(view.getUserPhone())
                .status(view.getStatus())
                .joinedAt(view.getJoinedAt())
                .suspendedAt(view.getSuspendedAt())
                .build();
    }

    private InstructorDetailRes toInstructorDetailRes(InstructorWithPermissionsView view) {
        return InstructorDetailRes.builder()
                .id(view.getId())
                .userId(view.getUserId())
                .name(view.getUserName())
                .email(view.getUserEmail())
                .phone(view.getUserPhone())
                .status(view.getStatus())
                .joinedAt(view.getJoinedAt())
                .suspendedAt(view.getSuspendedAt())
                .permissions(parsePermissions(view.getPermissions()))
                .build();
    }

    private Set<String> parsePermissions(String permissionsJson) {
        if (permissionsJson == null || permissionsJson.isBlank()) {
            return Set.of();
        }
        String cleanJson = permissionsJson.replaceAll("[\\[\\]\"]", "");
        if (cleanJson.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(cleanJson.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .filter(this::isValidPermissionType)
                .collect(Collectors.toSet());
    }

    private boolean isValidPermissionType(String name) {
        try {
            PermissionType.valueOf(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
