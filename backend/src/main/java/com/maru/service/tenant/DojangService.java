package com.maru.service.tenant;

import com.maru.common.exception.BusinessException;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.tenant.DojangRepository;
import com.maru.security.DojangAccessValidator;
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
}
