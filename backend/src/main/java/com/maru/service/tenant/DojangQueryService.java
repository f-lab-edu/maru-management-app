package com.maru.service.tenant;

import com.maru.common.exception.BusinessException;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.tenant.DojangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DojangQueryService {

    private final DojangRepository dojangRepository;

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
}
