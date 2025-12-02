package com.maru.service.tenant;

import com.maru.common.exception.BusinessException;
import com.maru.domain.employment.Employment;
import com.maru.domain.permission.PermissionType;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.Tenant;
import com.maru.domain.user.OnboardingStep;
import com.maru.domain.user.User;
import com.maru.domain.user.UserRole;
import com.maru.repository.employment.EmploymentRepository;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.tenant.TenantRepository;
import com.maru.service.tenant.search.MemorySearchStrategy;
import com.maru.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maru.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final UserService userService;
    private final TenantRepository tenantRepository;
    private final DojangRepository dojangRepository;
    private final EmploymentRepository employmentRepository;
    private final MemorySearchStrategy memorySearchStrategy;

    /**
     * 테넌트와 도장을 생성하고 관장 권한을 부여
     *
     * @param userId 사용자 ID
     * @param dojangName 도장 이름
     * @param address 도장 주소
     * @param phone 도장 전화번호
     * @return 생성된 도장 정보
     * @throws BusinessException USER_INVALID_ROLE - 사용자 역할이 OWNER가 아닌 경우
     * @throws BusinessException ONBOARDING_STAGE_INVALID - 온보딩 단계가 DOJANG_INFO가 아닌 경우
     */
    @Transactional
    public Dojang createTenantWithDojang(Long userId, String dojangName, String address, String phone) {
        User user = userService.getUserById(userId);
        validateOwnerRole(user);
        validateOnboardingStep(user);

        Tenant tenant = createTenant(user);
        Dojang dojang = createDojang(tenant, user, dojangName, address, phone);
        Employment employment = createOwnerEmployment(user, tenant, dojang);
        grantOwnerPermissions(employment);

        user.updateOnboardingStep(OnboardingStep.COMPLETED);

        memorySearchStrategy.addDojang(dojang);

        log.info("테넌트 및 도장 생성 완료: userId={}, tenantId={}, dojangId={}",
            userId, tenant.getId(), dojang.getId());

        return dojang;
    }

    private void validateOwnerRole(User user) {
        if (user.getRole() != UserRole.OWNER) {
            throw new BusinessException(USER_INVALID_ROLE);
        }
    }

    private void validateOnboardingStep(User user) {
        if (user.getOnboardingStep() != OnboardingStep.DOJANG_INFO) {
            throw new BusinessException(ONBOARDING_STAGE_INVALID);
        }
    }

    private Tenant createTenant(User owner) {
        Tenant tenant = Tenant.create(owner);
        return tenantRepository.save(tenant);
    }

    private Dojang createDojang(Tenant tenant, User owner, String name, String address, String phone) {
        Dojang dojang = Dojang.create(tenant, owner, name, address, phone);
        return dojangRepository.save(dojang);
    }

    private Employment createOwnerEmployment(User owner, Tenant tenant, Dojang dojang) {
        Employment employment = Employment.createForOwner(owner, tenant, dojang);
        return employmentRepository.save(employment);
    }

    private void grantOwnerPermissions(Employment employment) {
        PermissionType.getAllPermissions().forEach(employment::grantPermission);
    }
}
