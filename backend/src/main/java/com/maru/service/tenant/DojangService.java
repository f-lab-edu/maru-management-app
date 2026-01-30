package com.maru.service.tenant;

import com.maru.common.exception.BusinessException;
import com.maru.controller.tenant.dto.DojangUpdateReq;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.DojangSetting;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.domain.tenant.exception.DojangSettingErrorCode;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.tenant.DojangSettingRepository;
import com.maru.security.DojangAccessValidator;
import com.maru.security.TenantContextHolder;
import com.maru.service.search.dojang.DojangSearchIndexer;
import com.maru.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DojangService {

    private final DojangRepository dojangRepository;
    private final DojangSettingRepository dojangSettingRepository;
    private final DojangAccessValidator dojangAccessValidator;
    private final DojangSearchIndexer dojangSearchIndexer;
    private final UserService userService;

    /**
     * 도장 비활성화
     *
     * @param dojangId 도장 ID
     */
    @Transactional
    public void deactivate(String dojangId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        dojang.deactivate();
        dojangAccessValidator.evictDojangActiveCache(dojangId);
        dojangSearchIndexer.removeFromIndex(dojangId);

        log.info("도장 비활성화: dojangId={}", dojangId);
    }

    /**
     * 도장 활성화
     *
     * @param dojangId 도장 ID
     */
    @Transactional
    public void activate(String dojangId) {
        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        dojang.activate();
        dojangAccessValidator.evictDojangActiveCache(dojangId);

        String ownerName = userService.getUserById(dojang.getOwnerId()).getName();
        dojangSearchIndexer.addToIndex(dojang, ownerName);

        log.info("도장 활성화: dojangId={}", dojangId);
    }

    /**
     * 현재 도장 정보 및 수납 설정 수정
     *
     * @param request 수정할 도장 정보
     */
    @Transactional
    public void updateMyDojang(DojangUpdateReq request) {
        String dojangId = TenantContextHolder.getDojangId();

        Dojang dojang = dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));

        dojang.updateInfo(request.name(), request.address(), request.phone());

        DojangSetting setting = dojangSettingRepository.findByDojangId(dojangId)
                .orElseThrow(() -> new BusinessException(DojangSettingErrorCode.NOT_FOUND));

        setting.updateTuition(request.defaultTuition());
        setting.updateAutoInvoice(request.autoInvoiceEnabled(), request.autoInvoiceDay());

        log.info("도장 설정 수정: dojangId={}", dojangId);
    }
}
