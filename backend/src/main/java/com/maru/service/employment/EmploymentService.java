package com.maru.service.employment;

import com.maru.common.exception.BusinessException;
import com.maru.controller.employment.dto.EmploymentRes;
import com.maru.controller.employment.dto.InstructorDetailRes;
import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.employment.exception.EmploymentErrorCode;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.domain.user.OnboardingStep;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.tenant.view.DojangMinimalView;
import com.maru.security.DojangAccessValidator;
import com.maru.security.PermissionCache;
import com.maru.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmploymentService {

    private final EmploymentRepository employmentRepository;
    private final DojangRepository dojangRepository;
    private final UserService userService;
    private final EmploymentQueryService queryService;
    private final DojangAccessValidator dojangAccessValidator;
    private final PermissionCache permissionCache;

    /**
     * 사범이 도장에 승인 요청 (거절/퇴사 후 재요청 시 기존 레코드 재활용)
     *
     * @param userId 요청자(사범) ID
     * @param dojangId 도장 ID
     * @return 생성 또는 재활용된 Employment
     */
    @Transactional
    public EmploymentRes requestApproval(String userId, String dojangId) {
        DojangMinimalView dojang = dojangRepository.findMinimalById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        Employment employment = employmentRepository.findByUserIdAndDojangId(userId, dojangId)
                .map(existing -> handleExistingEmployment(existing, dojang.getName()))
                .orElseGet(() -> createNewEmployment(userId, dojang));

        return queryService.getEmployment(employment.getId());
    }

    private Employment handleExistingEmployment(Employment employment, String dojangName) {
        EmploymentStatus status = employment.getStatus();

        if (status == EmploymentStatus.REJECTED || status == EmploymentStatus.LEFT) {
            employment.rejoin();
            log.info("승인 재요청 (rejoin): employmentId={}, userId={}, dojangName={}",
                    employment.getId(), employment.getUserId(), dojangName);
            return employment;
        }

        log.warn("재요청 불가: employmentId={}, status={}", employment.getId(), status);
        throw new BusinessException(EmploymentErrorCode.ALREADY_EXISTS);
    }

    private Employment createNewEmployment(String userId, DojangMinimalView dojang) {
        Employment employment = Employment.create(userId, dojang.getTenantId(), dojang.getId());
        Employment saved = employmentRepository.save(employment);

        log.info("승인 요청 생성: employmentId={}, userId={}, dojangId={}, dojangName={}",
                saved.getId(), userId, dojang.getId(), dojang.getName());
        return saved;
    }

    /**
     * 승인 요청 승인 (관장용)
     *
     * @param employmentId 승인 요청 ID
     * @param ownerId 관장 ID
     * @return 승인된 Employment
     */
    @Transactional
    public EmploymentRes approve(String employmentId, String ownerId) {
        Employment employment = getEmploymentById(employmentId);
        validateOwnerPermission(employment, ownerId);
        validatePendingStatus(employment);

        employment.approve();
        grantDefaultPermissions(employment);

        userService.updateOnboardingStep(employment.getUserId(), OnboardingStep.COMPLETED);

        evictCache(employment);

        log.info("승인 요청 승인: employmentId={}, ownerId={}, userId={}",
                employmentId, ownerId, employment.getUserId());
        return queryService.getEmployment(employmentId);
    }

    /**
     * 승인 요청 거절 (관장용)
     *
     * @param employmentId 승인 요청 ID
     * @param ownerId 관장 ID
     * @return 거절된 Employment
     */
    @Transactional
    public EmploymentRes reject(String employmentId, String ownerId) {
        Employment employment = getEmploymentById(employmentId);
        validateOwnerPermission(employment, ownerId);
        validatePendingStatus(employment);

        employment.reject();

        log.info("승인 요청 거절: employmentId={}, ownerId={}, userId={}",
                employmentId, ownerId, employment.getUserId());
        return queryService.getEmployment(employmentId);
    }

    /**
     * 승인 요청 취소 (사범 본인용)
     *
     * @param employmentId 승인 요청 ID
     * @param userId 요청자 ID
     */
    @Transactional
    public void cancel(String employmentId, String userId) {
        Employment employment = getEmploymentById(employmentId);
        validateRequesterPermission(employment, userId);
        validatePendingStatus(employment);

        employmentRepository.delete(employment);
        log.info("승인 요청 취소: employmentId={}, userId={}, dojangId={}",
                employmentId, userId, employment.getDojangId());
    }

    private void validateRequesterPermission(Employment employment, String userId) {
        if (!employment.getUserId().equals(userId)) {
            log.warn("본인이 아닌 사용자가 취소 시도: employmentId={}, requesterId={}, actualUserId={}",
                    employment.getId(), userId, employment.getUserId());
            throw new BusinessException(EmploymentErrorCode.NOT_REQUESTER);
        }
    }

    private Employment getEmploymentById(String employmentId) {
        return employmentRepository.findById(employmentId)
                .orElseThrow(() -> new BusinessException(EmploymentErrorCode.NOT_FOUND));
    }

    private void validateOwnerPermission(Employment employment, String ownerId) {
        String actualOwnerId = dojangRepository.findOwnerIdById(employment.getDojangId())
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        if (!actualOwnerId.equals(ownerId)) {
            log.warn("권한 없는 승인/거절 시도: employmentId={}, requesterId={}, actualOwnerId={}",
                    employment.getId(), ownerId, actualOwnerId);
            throw new BusinessException(EmploymentErrorCode.NOT_OWNER);
        }
    }

    private void validatePendingStatus(Employment employment) {
        if (employment.getStatus() != EmploymentStatus.PENDING) {
            log.warn("PENDING 상태가 아닌 요청 처리 시도: employmentId={}, currentStatus={}",
                    employment.getId(), employment.getStatus());
            throw new BusinessException(EmploymentErrorCode.NOT_PENDING);
        }
    }

    /**
     * 사범 권한 수정 (관장용)
     *
     * @param employmentId 고용 ID
     * @param permissionNames 새로운 권한 이름 목록
     * @param ownerId 관장 ID
     * @return 수정된 사범 상세 정보
     */
    @Transactional
    public InstructorDetailRes updatePermissions(String employmentId, Set<String> permissionNames, String ownerId) {
        Employment employment = getEmploymentById(employmentId);
        validateInstructorModification(employment, ownerId);

        Set<PermissionType> permissions = toPermissionTypes(permissionNames);
        employment.replacePermissions(permissions);
        evictCache(employment);

        log.info("사범 권한 수정: employmentId={}, ownerId={}, permissions={}",
                employmentId, ownerId, permissions);
        return queryService.getInstructorDetail(employmentId);
    }

    /**
     * 사범 권한 기본값 초기화 (관장용)
     *
     * @param employmentId 고용 ID
     * @param ownerId 관장 ID
     * @return 초기화된 사범 상세 정보
     */
    @Transactional
    public InstructorDetailRes resetPermissions(String employmentId, String ownerId) {
        Employment employment = getEmploymentById(employmentId);
        validateInstructorModification(employment, ownerId);

        employment.resetToDefaultPermissions();
        evictCache(employment);

        log.info("사범 권한 초기화: employmentId={}, ownerId={}", employmentId, ownerId);
        return queryService.getInstructorDetail(employmentId);
    }

    /**
     * 사범 상태 변경 (관장용)
     *
     * @param employmentId 고용 ID
     * @param statusName 변경할 상태 이름
     * @param reason 변경 사유
     * @param ownerId 관장 ID
     * @return 변경된 사범 상세 정보
     */
    @Transactional
    public InstructorDetailRes updateInstructorStatus(String employmentId, String statusName, String reason, String ownerId) {
        Employment employment = getEmploymentById(employmentId);
        validateInstructorModification(employment, ownerId);

        EmploymentStatus status = EmploymentStatus.valueOf(statusName);
        applyStatusTransition(employment, status);
        evictCache(employment);

        log.info("사범 상태 변경: employmentId={}, status={}, reason={}, ownerId={}",
                employmentId, status, reason, ownerId);
        return queryService.getInstructorDetail(employmentId);
    }

    private void validateInstructorModification(Employment employment, String ownerId) {
        validateOwnerPermission(employment, ownerId);
        validateNotOwner(employment);
    }

    private void applyStatusTransition(Employment employment, EmploymentStatus status) {
        if (status == EmploymentStatus.SUSPENDED) {
            employment.suspend();
        } else if (status == EmploymentStatus.ACTIVE) {
            employment.reactivate();
        } else {
            throw new BusinessException(EmploymentErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    private void evictCache(Employment employment) {
        String userId = employment.getUserId();
        String tenantId = employment.getTenantId();
        String dojangId = employment.getDojangId();

        dojangAccessValidator.evictEmploymentCache(userId, dojangId);
        permissionCache.invalidate(userId, tenantId, dojangId);
    }

    private Set<PermissionType> toPermissionTypes(Set<String> permissionNames) {
        return permissionNames.stream()
                .map(PermissionType::valueOf)
                .collect(Collectors.toSet());
    }

    private void validateNotOwner(Employment employment) {
        String actualOwnerId = dojangRepository.findOwnerIdById(employment.getDojangId())
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        if (actualOwnerId.equals(employment.getUserId())) {
            log.warn("관장 자기수정 시도: employmentId={}, userId={}",
                    employment.getId(), employment.getUserId());
            throw new BusinessException(EmploymentErrorCode.CANNOT_MODIFY_OWNER);
        }
    }

    private void grantDefaultPermissions(Employment employment) {
        var defaultPermissions = PermissionType.getDefaultPermissions();
        defaultPermissions.forEach(employment::grantPermission);

        log.info("기본 권한 부여 완료: employmentId={}, userId={}, permissions={}",
                employment.getId(),
                employment.getUserId(),
                defaultPermissions);
    }
}
