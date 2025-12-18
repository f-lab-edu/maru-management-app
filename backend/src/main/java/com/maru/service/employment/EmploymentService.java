package com.maru.service.employment;

import com.maru.common.exception.BusinessException;
import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.employment.exception.EmploymentErrorCode;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.domain.user.OnboardingStep;
import com.maru.domain.user.User;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmploymentService {

    private final EmploymentRepository employmentRepository;
    private final DojangRepository dojangRepository;
    private final UserService userService;

    /**
     * 사범이 도장에 승인 요청 (거절/퇴사 후 재요청 시 기존 레코드 재활용)
     *
     * @param userId 요청자(사범) ID
     * @param dojangId 도장 ID
     * @return 생성 또는 재활용된 Employment
     */
    @Transactional
    public Employment requestApproval(Long userId, Long dojangId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        return employmentRepository.findByUserIdAndDojangId(userId, dojangId)
                .map(existing -> handleExistingEmployment(existing, dojang.getName()))
                .orElseGet(() -> createNewEmployment(userId, dojang));
    }

    private Employment handleExistingEmployment(Employment employment, String dojangName) {
        EmploymentStatus status = employment.getStatus();

        if (status == EmploymentStatus.REJECTED || status == EmploymentStatus.LEFT) {
            employment.rejoin();
            log.info("승인 재요청 (rejoin): employmentId={}, userId={}, dojangName={}",
                    employment.getId(), employment.getUser().getId(), dojangName);
            return employment;
        }

        log.warn("재요청 불가: employmentId={}, status={}", employment.getId(), status);
        throw new BusinessException(EmploymentErrorCode.ALREADY_EXISTS);
    }

    private Employment createNewEmployment(Long userId, Dojang dojang) {
        User user = userService.getUserById(userId);
        Employment employment = Employment.create(user, dojang.getTenant(), dojang);
        Employment saved = employmentRepository.save(employment);

        log.info("승인 요청 생성: employmentId={}, userId={}, dojangId={}, dojangName={}",
                saved.getId(), userId, dojang.getId(), dojang.getName());
        return saved;
    }

    /**
     * 도장의 대기 중인 승인 요청 목록 조회 (관장용)
     *
     * @param dojangId 도장 ID
     * @return 대기 중인 Employment 목록
     */
    @Transactional(readOnly = true)
    public List<Employment> getPendingRequests(Long dojangId) {
        return employmentRepository.findByDojangIdAndStatus(dojangId, EmploymentStatus.PENDING);
    }

    /**
     * 사용자의 모든 승인 요청 조회 (사범용)
     *
     * @param userId 사용자 ID
     * @return Employment 목록
     */
    @Transactional(readOnly = true)
    public List<Employment> getMyRequests(Long userId) {
        return employmentRepository.findByUserId(userId);
    }

    /**
     * 사용자가 속한 도장 목록 조회 (활성 고용만)
     *
     * @param userId 사용자 ID
     * @return Employment 목록
     */
    @Transactional(readOnly = true)
    public List<Employment> getMyDojangs(Long userId) {
        return employmentRepository.findActiveWithDojangAndTenant(userId, EmploymentStatus.ACTIVE);
    }

    /**
     * 승인 요청 승인 (관장용)
     *
     * @param employmentId 승인 요청 ID
     * @param ownerId 관장 ID
     * @return 승인된 Employment
     */
    @Transactional
    public Employment approve(Long employmentId, Long ownerId) {
        Employment employment = getEmploymentById(employmentId);
        validateOwnerPermission(employment, ownerId);
        validatePendingStatus(employment);

        employment.approve();
        grantDefaultPermissions(employment);

        employment.getUser().updateOnboardingStep(OnboardingStep.COMPLETED);

        log.info("승인 요청 승인: employmentId={}, ownerId={}, userId={}",
                employmentId, ownerId, employment.getUser().getId());
        return employment;
    }

    /**
     * 승인 요청 거절 (관장용)
     *
     * @param employmentId 승인 요청 ID
     * @param ownerId 관장 ID
     * @return 거절된 Employment
     */
    @Transactional
    public Employment reject(Long employmentId, Long ownerId) {
        Employment employment = getEmploymentById(employmentId);
        validateOwnerPermission(employment, ownerId);
        validatePendingStatus(employment);

        employment.reject();

        log.info("승인 요청 거절: employmentId={}, ownerId={}, userId={}",
                employmentId, ownerId, employment.getUser().getId());
        return employment;
    }

    /**
     * 승인 요청 취소 (사범 본인용)
     *
     * @param employmentId 승인 요청 ID
     * @param userId 요청자 ID
     */
    @Transactional
    public void cancel(Long employmentId, Long userId) {
        Employment employment = getEmploymentById(employmentId);
        validateRequesterPermission(employment, userId);
        validatePendingStatus(employment);

        employmentRepository.delete(employment);
        log.info("승인 요청 취소: employmentId={}, userId={}, dojangId={}",
                employmentId, userId, employment.getDojang().getId());
    }

    private void validateRequesterPermission(Employment employment, Long userId) {
        if (!employment.getUser().getId().equals(userId)) {
            log.warn("본인이 아닌 사용자가 취소 시도: employmentId={}, requesterId={}, actualUserId={}",
                    employment.getId(), userId, employment.getUser().getId());
            throw new BusinessException(EmploymentErrorCode.NOT_REQUESTER);
        }
    }

    private Employment getEmploymentById(Long employmentId) {
        return employmentRepository.findById(employmentId)
                .orElseThrow(() -> new BusinessException(EmploymentErrorCode.NOT_FOUND));
    }

    private void validateOwnerPermission(Employment employment, Long ownerId) {
        Long dojangOwnerId = employment.getDojang().getOwner().getId();
        if (!dojangOwnerId.equals(ownerId)) {
            log.warn("권한 없는 승인/거절 시도: employmentId={}, requesterId={}, actualOwnerId={}",
                    employment.getId(), ownerId, dojangOwnerId);
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

    private void grantDefaultPermissions(Employment employment) {
        var defaultPermissions = PermissionType.getDefaultPermissions();
        defaultPermissions.forEach(employment::grantPermission);

        log.info("기본 권한 부여 완료: employmentId={}, userId={}, permissions={}",
                employment.getId(),
                employment.getUser().getId(),
                defaultPermissions);
    }
}
