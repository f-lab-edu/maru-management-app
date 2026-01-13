package com.maru.service.section;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.domain.permission.PermissionType;
import com.maru.security.RequirePermission;
import com.maru.controller.section.dto.DivisionDetailRes;
import com.maru.controller.section.dto.DivisionListRes;
import com.maru.controller.section.dto.DivisionRes;
import com.maru.domain.section.exception.DivisionErrorCode;
import com.maru.repository.section.DivisionRepository;
import com.maru.repository.section.view.DivisionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ValidateDojangAccess
public class DivisionQueryService {

    private final DivisionRepository divisionRepository;

    /**
     * 수련반 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID
     * @return 수련반 목록 (displayOrder 순)
     */
    @RequirePermission(PermissionType.DOJANG_MANAGE_CLASS)
    public DivisionListRes getDivisions(String dojangId, String sectionId) {
        List<DivisionView> views = divisionRepository.findAllWithStudentCount(dojangId, sectionId);

        List<DivisionRes> divisions = views.stream()
                .map(this::toDivisionRes)
                .toList();

        return DivisionListRes.from(divisions);
    }

    /**
     * 수련반 단건 조회
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 수련반 정보
     * @throws BusinessException NOT_FOUND - 수련반을 찾을 수 없는 경우
     */
    @RequirePermission(PermissionType.DOJANG_MANAGE_CLASS)
    public DivisionRes getDivision(String dojangId, String divisionId) {
        DivisionView view = divisionRepository.findDetailById(divisionId, dojangId)
                .orElseThrow(() -> new BusinessException(DivisionErrorCode.NOT_FOUND));

        return toDivisionRes(view);
    }

    /**
     * 수련반 상세 조회
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 수련반 상세 정보
     * @throws BusinessException NOT_FOUND - 수련반을 찾을 수 없는 경우
     */
    @RequirePermission(PermissionType.DOJANG_MANAGE_CLASS)
    public DivisionDetailRes getDivisionDetail(String dojangId, String divisionId) {
        DivisionView view = divisionRepository.findDetailById(divisionId, dojangId)
                .orElseThrow(() -> new BusinessException(DivisionErrorCode.NOT_FOUND));

        return toDivisionDetailRes(view);
    }

    private DivisionRes toDivisionRes(DivisionView view) {
        return DivisionRes.builder()
                .id(view.getId())
                .sectionId(view.getSectionId())
                .sectionName(view.getSectionName())
                .name(view.getName())
                .displayOrder(view.getDisplayOrder())
                .scheduleDays(view.getScheduleDays())
                .startTime(view.getStartTime())
                .endTime(view.getEndTime())
                .studentCount(view.getStudentCount().intValue())
                .build();
    }

    private DivisionDetailRes toDivisionDetailRes(DivisionView view) {
        return DivisionDetailRes.builder()
                .id(view.getId())
                .sectionId(view.getSectionId())
                .sectionName(view.getSectionName())
                .name(view.getName())
                .displayOrder(view.getDisplayOrder())
                .scheduleDays(view.getScheduleDays())
                .startTime(view.getStartTime())
                .endTime(view.getEndTime())
                .studentCount(view.getStudentCount().intValue())
                .build();
    }
}
