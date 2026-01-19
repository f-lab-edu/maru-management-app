package com.maru.service.section;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.domain.permission.PermissionType;
import com.maru.security.RequirePermission;
import com.maru.controller.section.dto.DivisionCreateReq;
import com.maru.controller.section.dto.DivisionReorderReq;
import com.maru.controller.section.dto.DivisionRes;
import com.maru.controller.section.dto.DivisionUpdateReq;
import com.maru.domain.section.Division;
import com.maru.domain.section.exception.DivisionErrorCode;
import com.maru.domain.section.Section;
import com.maru.domain.section.exception.SectionErrorCode;
import com.maru.repository.section.DivisionRepository;
import com.maru.repository.section.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ValidateDojangAccess
public class DivisionService {

    private final DivisionRepository divisionRepository;
    private final SectionRepository sectionRepository;
    private final DivisionQueryService divisionQueryService;

    /**
     * 수련반 생성
     *
     * @param dojangId 도장 ID
     * @param request 생성 요청
     * @return 생성된 수련반 정보
     * @throws BusinessException 수련부를 찾을 수 없거나 중복 이름인 경우
     */
    @RequirePermission(PermissionType.DOJANG_MANAGE_CLASS)
    @Transactional
    public DivisionRes createDivision(String dojangId, DivisionCreateReq request) {
        Section section = findSectionByIdAndDojangId(request.sectionId(), dojangId);

        validateDuplicateName(request.sectionId(), request.name());

        int nextDisplayOrder = divisionRepository.findMaxDisplayOrderBySectionId(request.sectionId()) + 1;
        Division division = Division.create(dojangId, section, request.name(), nextDisplayOrder);

        if (request.scheduleDays() != null && !request.scheduleDays().isEmpty()) {
            division.updateSchedule(request.scheduleDays(), request.startTime(), request.endTime());
        }

        divisionRepository.save(division);

        return divisionQueryService.getDivision(dojangId, division.getId());
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
    @RequirePermission(PermissionType.DOJANG_MANAGE_CLASS)
    @Transactional
    public DivisionRes updateDivision(String dojangId, String divisionId, DivisionUpdateReq request) {
        Division division = findDivisionByIdAndDojangId(divisionId, dojangId);

        validateDuplicateNameExcludingSelf(division.getSection().getId(), request.name(), divisionId);

        division.updateName(request.name());
        division.updateSchedule(request.scheduleDays(), request.startTime(), request.endTime());

        return divisionQueryService.getDivision(dojangId, divisionId);
    }

    /**
     * 수련반 삭제
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @throws BusinessException 수련반을 찾을 수 없는 경우
     */
    @RequirePermission(PermissionType.DOJANG_MANAGE_CLASS)
    @Transactional
    public void deleteDivision(String dojangId, String divisionId) {
        Division division = findDivisionByIdAndDojangId(divisionId, dojangId);
        divisionRepository.delete(division);
    }

    /**
     * 수련반 순서 변경
     *
     * @param dojangId 도장 ID
     * @param request 순서 변경 요청 (수련부 ID, 수련반 ID 목록)
     * @throws BusinessException 중복 ID, 개수 불일치, 수련반 미존재 시
     */
    @RequirePermission(PermissionType.DOJANG_MANAGE_CLASS)
    @Transactional
    public void reorderDivisions(String dojangId, DivisionReorderReq request) {
        findSectionByIdAndDojangId(request.sectionId(), dojangId);

        List<String> divisionIds = request.divisionIds();
        validateNoDuplicateIds(divisionIds);

        List<Division> allDivisions = findAllDivisionsAndValidateCount(request.sectionId(), divisionIds);
        updateDivisionDisplayOrders(allDivisions, divisionIds);
    }

    private void validateNoDuplicateIds(List<String> divisionIds) {
        Set<String> divisionIdSet = new HashSet<>(divisionIds);
        if (divisionIdSet.size() != divisionIds.size()) {
            throw new BusinessException(DivisionErrorCode.DUPLICATE_ID_IN_REQUEST);
        }
    }

    private List<Division> findAllDivisionsAndValidateCount(String sectionId, List<String> divisionIds) {
        List<Division> allDivisions = divisionRepository.findAllBySectionIdOrderByDisplayOrder(sectionId);
        if (allDivisions.size() != divisionIds.size()) {
            throw new BusinessException(DivisionErrorCode.REORDER_COUNT_MISMATCH);
        }
        return allDivisions;
    }

    private void updateDivisionDisplayOrders(List<Division> allDivisions, List<String> divisionIds) {
        Map<String, Division> divisionMap = allDivisions.stream()
                .collect(Collectors.toMap(Division::getId, Function.identity()));

        for (int i = 0; i < divisionIds.size(); i++) {
            Division division = divisionMap.get(divisionIds.get(i));
            if (division == null) {
                throw new BusinessException(DivisionErrorCode.NOT_FOUND);
            }
            division.updateDisplayOrder(i);
        }
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
}
