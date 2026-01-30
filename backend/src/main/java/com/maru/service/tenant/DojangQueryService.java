package com.maru.service.tenant;

import com.maru.common.exception.BusinessException;
import com.maru.controller.tenant.dto.DojangMeRes;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.DojangSetting;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.domain.tenant.exception.DojangSettingErrorCode;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.tenant.DojangSettingRepository;
import com.maru.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DojangQueryService {

    private final DojangRepository dojangRepository;
    private final DojangSettingRepository dojangSettingRepository;

    /**
     * 관장 ID로 도장 ID 조회
     *
     * @param ownerId 관장(소유자) ID
     * @return 도장 ID
     * @throws BusinessException NOT_FOUND - 해당 관장의 도장이 존재하지 않는 경우
     */
    public String getDojangIdByOwnerId(String ownerId) {
        return dojangRepository.findIdByOwnerId(ownerId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));
    }

    /**
     * 현재 사용자의 도장 정보 + 설정 조회
     *
     * @return 도장 정보 및 수납 설정
     */
    public DojangMeRes getMyDojang() {
        String dojangId = TenantContextHolder.getDojangId();

        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        DojangSetting setting = dojangSettingRepository.findByDojangId(dojangId)
                .orElseThrow(() -> new BusinessException(DojangSettingErrorCode.NOT_FOUND));

        return DojangMeRes.from(dojang, setting);
    }
}
