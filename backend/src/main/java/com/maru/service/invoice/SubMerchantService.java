package com.maru.service.invoice;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.controller.invoice.dto.SubMerchantCreateReq;
import com.maru.controller.invoice.dto.SubMerchantRes;
import com.maru.controller.invoice.dto.SubMerchantStatusUpdateReq;
import com.maru.domain.invoice.SubMerchant;
import com.maru.domain.invoice.exception.SubMerchantErrorCode;
import com.maru.domain.permission.PermissionType;
import com.maru.repository.invoice.SubMerchantRepository;
import com.maru.security.RequirePermission;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ValidateDojangAccess
public class SubMerchantService {

    private final SubMerchantRepository subMerchantRepository;

    /**
     * 서브몰 등록 (DB 저장만 수행, 토스 API 호출 없음)
     *
     * @param dojangId 도장 ID
     * @param request 서브몰 등록 요청
     * @return 등록된 서브몰 정보
     * @throws BusinessException ALREADY_REGISTERED - 이미 등록된 서브몰
     */
    @RequirePermission(PermissionType.DOJANG_UPDATE_INFO)
    @Transactional
    public SubMerchantRes register(String dojangId, SubMerchantCreateReq request) {
        String tenantId = TenantContextHolder.getTenantId();
        validateNotRegistered(dojangId);

        SubMerchant subMerchant = SubMerchant.create(
                tenantId, dojangId, request.feeRate(),
                request.bankCode(), request.accountNumber(), request.accountHolder()
        );
        subMerchantRepository.save(subMerchant);

        log.info("서브몰 등록 완료: dojangId={}", dojangId);
        return SubMerchantRes.from(subMerchant);
    }

    /**
     * 서브몰 조회
     *
     * @param dojangId 도장 ID
     * @return 서브몰 정보
     * @throws BusinessException NOT_FOUND - 서브몰 미등록
     */
    @RequirePermission(PermissionType.DOJANG_UPDATE_INFO)
    @Transactional(readOnly = true)
    public SubMerchantRes getByDojangId(String dojangId) {
        SubMerchant subMerchant = findByDojangId(dojangId);
        return SubMerchantRes.from(subMerchant);
    }

    /**
     * 서브몰 상태 변경 (관리자)
     *
     * @param dojangId 도장 ID
     * @param request 상태 변경 요청
     * @return 변경된 서브몰 정보
     * @throws BusinessException INVALID_STATUS_TRANSITION - 유효하지 않은 상태 전이
     */
    @RequirePermission(PermissionType.DOJANG_UPDATE_INFO)
    @Transactional
    public SubMerchantRes updateStatus(String dojangId, SubMerchantStatusUpdateReq request) {
        SubMerchant subMerchant = findByDojangId(dojangId);

        switch (request.status()) {
            case ACTIVE -> subMerchant.activate(subMerchant.getTossSellerId());
            case REJECTED -> subMerchant.reject();
            case SUSPENDED -> subMerchant.suspend();
            default -> throw new BusinessException(SubMerchantErrorCode.INVALID_STATUS_TRANSITION);
        }

        log.info("서브몰 상태 변경: dojangId={}, status={}", dojangId, request.status());
        return SubMerchantRes.from(subMerchant);
    }

    private SubMerchant findByDojangId(String dojangId) {
        return subMerchantRepository.findByDojangId(dojangId)
                .orElseThrow(() -> new BusinessException(SubMerchantErrorCode.NOT_FOUND));
    }

    private void validateNotRegistered(String dojangId) {
        subMerchantRepository.findByDojangId(dojangId)
                .ifPresent(sm -> {
                    throw new BusinessException(SubMerchantErrorCode.ALREADY_REGISTERED);
                });
    }
}
