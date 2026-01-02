package com.maru.service.division;

import com.maru.common.exception.BusinessException;
import com.maru.controller.division.dto.DivisionCreateReq;
import com.maru.controller.division.dto.DivisionDetailRes;
import com.maru.controller.division.dto.DivisionListRes;
import com.maru.controller.division.dto.DivisionRes;
import com.maru.controller.division.dto.DivisionUpdateReq;
import com.maru.domain.division.Division;
import com.maru.domain.division.exception.DivisionErrorCode;
import com.maru.domain.section.Section;
import com.maru.domain.section.exception.SectionErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.division.DivisionRepository;
import com.maru.repository.section.SectionRepository;
import com.maru.repository.tenant.DojangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DivisionService {

    private final DivisionRepository divisionRepository;
    private final SectionRepository sectionRepository;
    private final DojangRepository dojangRepository;

    /**
     * 수련반 생성
     *
     * @param dojangId 도장 ID
     * @param request 생성 요청
     * @return 생성된 수련반 정보
     * @throws BusinessException 수련부를 찾을 수 없거나 중복 이름인 경우
     */
    @Transactional
    public DivisionRes createDivision(String dojangId, DivisionCreateReq request) {
        Dojang dojang = findDojangById(dojangId);
        Section section = findSectionByIdAndDojangId(request.sectionId(), dojangId);

        validateDuplicateName(request.sectionId(), request.name());

        int nextDisplayOrder = divisionRepository.findMaxDisplayOrderBySectionId(request.sectionId()) + 1;
        Division division = Division.create(dojang, section, request.name(), nextDisplayOrder);

        if (request.dayOfWeek() != null) {
            division.updateSchedule(request.dayOfWeek(), request.startTime(), request.endTime());
        }

        divisionRepository.save(division);

        return toDivisionRes(division);
    }

    /**
     * 수련반 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID (선택, null이면 전체)
     * @return 수련반 목록 (section.displayOrder, displayOrder 순)
     */
    public DivisionListRes getDivisions(String dojangId, String sectionId) {
        List<Division> divisions = (sectionId == null)
                ? divisionRepository.findAllWithSectionByDojangId(dojangId)
                : divisionRepository.findAllWithSectionByDojangIdAndSectionId(dojangId, sectionId);

        List<DivisionRes> divisionResList = divisions.stream()
                .map(this::toDivisionRes)
                .toList();

        return DivisionListRes.from(divisionResList);
    }

    /**
     * 수련반 상세
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 수련반 상세 정보
     * @throws BusinessException 수련반을 찾을 수 없는 경우
     */
    public DivisionDetailRes getDivisionDetail(String dojangId, String divisionId) {
        Division division = findDivisionByIdAndDojangId(divisionId, dojangId);
        return toDivisionDetailRes(division);
    }

    /**
     * 수련반 정보 수정
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param request 수정 요청
     * @return 수정된 수련반 정보
     * @throws BusinessException 수련반을 찾을 수 없거나 중복 이름인 경우
     */
    @Transactional
    public DivisionRes updateDivision(String dojangId, String divisionId, DivisionUpdateReq request) {
        Division division = findDivisionByIdAndDojangId(divisionId, dojangId);

        validateDuplicateNameExcludingSelf(division.getSection().getId(), request.name(), divisionId);

        division.updateName(request.name());
        division.updateSchedule(request.dayOfWeek(), request.startTime(), request.endTime());

        return toDivisionRes(division);
    }

    /**
     * 수련반 삭제
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @throws BusinessException 수련반을 찾을 수 없는 경우
     */
    @Transactional
    public void deleteDivision(String dojangId, String divisionId) {
        Division division = findDivisionByIdAndDojangId(divisionId, dojangId);
        division.markAsDeleted();
    }

    private Dojang findDojangById(String dojangId) {
        return dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));
    }

    private Section findSectionByIdAndDojangId(String sectionId, String dojangId) {
        return sectionRepository.findByIdAndDojangId(sectionId, dojangId)
                .orElseThrow(() -> new BusinessException(SectionErrorCode.NOT_FOUND));
    }

    private Division findDivisionByIdAndDojangId(String divisionId, String dojangId) {
        return divisionRepository.findByIdAndDojangIdWithSection(divisionId, dojangId)
                .orElseThrow(() -> new BusinessException(DivisionErrorCode.NOT_FOUND));
    }

    private void validateDuplicateName(String sectionId, String name) {
        if (divisionRepository.existsBySectionIdAndName(sectionId, name)) {
            throw new BusinessException(DivisionErrorCode.DUPLICATE_NAME);
        }
    }

    private void validateDuplicateNameExcludingSelf(String sectionId, String name, String excludeId) {
        if (divisionRepository.existsBySectionIdAndNameAndIdNot(sectionId, name, excludeId)) {
            throw new BusinessException(DivisionErrorCode.DUPLICATE_NAME);
        }
    }

    private DivisionRes toDivisionRes(Division division) {
        return DivisionRes.builder()
                .id(division.getId())
                .sectionId(division.getSection().getId())
                .sectionName(division.getSection().getName())
                .name(division.getName())
                .displayOrder(division.getDisplayOrder())
                .dayOfWeek(division.getDayOfWeek())
                .startTime(division.getStartTime())
                .endTime(division.getEndTime())
                .studentCount(0)
                .build();
    }

    private DivisionDetailRes toDivisionDetailRes(Division division) {
        return DivisionDetailRes.builder()
                .id(division.getId())
                .sectionId(division.getSection().getId())
                .sectionName(division.getSection().getName())
                .name(division.getName())
                .displayOrder(division.getDisplayOrder())
                .dayOfWeek(division.getDayOfWeek())
                .startTime(division.getStartTime())
                .endTime(division.getEndTime())
                .studentCount(0)
                .build();
    }
}
