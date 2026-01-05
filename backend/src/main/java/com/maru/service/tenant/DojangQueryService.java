package com.maru.service.tenant;

import com.maru.common.exception.BusinessException;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.tenant.DojangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 도장 조회 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DojangQueryService {

    private final DojangRepository dojangRepository;

    /**
     * 관장 ID로 도장 조회
     *
     * @param ownerId 관장(소유자) ID
     * @return 도장 엔티티
     * @throws BusinessException NOT_FOUND - 해당 관장의 도장이 존재하지 않는 경우
     */
    public Dojang getByOwnerId(String ownerId) {
        return dojangRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));
    }
}
