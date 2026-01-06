package com.maru.service.guardian;

import com.maru.common.exception.BusinessException;
import com.maru.controller.student.dto.GuardianRes;
import com.maru.domain.guardian.exception.GuardianErrorCode;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.guardian.view.GuardianshipView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuardianQueryService {

    private final GuardianshipRepository guardianshipRepository;

    /**
     * 원생의 보호자 목록 조회
     *
     * @param studentId 원생 ID
     * @return 보호자 목록
     */
    public List<GuardianRes> getGuardiansByStudentId(String studentId) {
        return guardianshipRepository.findGuardianshipsByStudentId(studentId)
                .stream()
                .map(this::toGuardianRes)
                .toList();
    }

    /**
     * 특정 보호자 단건 조회
     *
     * @param studentId 원생 ID
     * @param guardianId 보호자 ID
     * @return 보호자 정보
     */
    public GuardianRes getGuardian(String studentId, String guardianId) {
        return guardianshipRepository.findGuardianshipByStudentIdAndGuardianId(studentId, guardianId)
                .map(this::toGuardianRes)
                .orElseThrow(() -> new BusinessException(GuardianErrorCode.NOT_FOUND));
    }

    private GuardianRes toGuardianRes(GuardianshipView view) {
        return GuardianRes.builder()
                .id(view.getGuardianId())
                .name(view.getGuardianName())
                .phone(view.getGuardianPhone())
                .relation(view.getRelation())
                .isPrimary(view.getIsPrimary())
                .isVerified(view.getIsVerified())
                .build();
    }
}
