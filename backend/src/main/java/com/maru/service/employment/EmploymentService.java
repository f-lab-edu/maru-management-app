package com.maru.service.employment;

import com.maru.common.exception.BusinessException;
import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.tenant.Dojang;
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

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmploymentService {

    private final EmploymentRepository employmentRepository;
    private final DojangRepository dojangRepository;
    private final UserService userService;

    /**
     * 사범이 도장에 승인 요청
     *
     * @param userId 요청자(사범) ID
     * @param dojangId 도장 ID
     * @return 생성된 Employment
     */
    @Transactional
    public Employment requestApproval(Long userId, Long dojangId) {
        if (employmentRepository.existsByUserIdAndDojangId(userId, dojangId)) {
            throw new BusinessException(EMPLOYMENT_ALREADY_EXISTS);
        }

        User user = userService.getUserById(userId);
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DOJANG_NOT_FOUND));

        Employment employment = Employment.create(user, dojang.getTenant(), dojang);
        Employment saved = employmentRepository.save(employment);

        log.info("승인 요청 생성: userId={}, dojangId={}, employmentId={}", userId, dojangId, saved.getId());
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

        employment.approve();
        grantDefaultPermissions(employment);

        employment.getUser().updateOnboardingStep(OnboardingStep.COMPLETED);

        log.info("승인 요청 승인: employmentId={}, ownerId={}", employmentId, ownerId);
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

        employment.reject();

        log.info("승인 요청 거절: employmentId={}, ownerId={}", employmentId, ownerId);
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

        if (!employment.getUser().getId().equals(userId)) {
            throw new BusinessException(EMPLOYMENT_NOT_REQUESTER);
        }

        if (employment.getStatus() != EmploymentStatus.PENDING) {
            throw new BusinessException(EMPLOYMENT_NOT_PENDING);
        }

        employmentRepository.delete(employment);
        log.info("승인 요청 취소: employmentId={}, userId={}", employmentId, userId);
    }

    private Employment getEmploymentById(Long employmentId) {
        return employmentRepository.findById(employmentId)
                .orElseThrow(() -> new BusinessException(EMPLOYMENT_NOT_FOUND));
    }

    private void validateOwnerPermission(Employment employment, Long ownerId) {
        Long dojangOwnerId = employment.getDojang().getOwner().getId();
        if (!dojangOwnerId.equals(ownerId)) {
            throw new BusinessException(EMPLOYMENT_NOT_OWNER);
        }
    }

    private void grantDefaultPermissions(Employment employment) {
        PermissionType.getDefaultPermissions().forEach(employment::grantPermission);
    }
}
