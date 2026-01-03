package com.maru.service.division;

import com.maru.common.exception.BusinessException;
import com.maru.controller.division.dto.DivisionCreateReq;
import com.maru.controller.division.dto.DivisionDetailRes;
import com.maru.controller.division.dto.DivisionListRes;
import com.maru.controller.division.dto.DivisionReorderReq;
import com.maru.controller.division.dto.DivisionRes;
import com.maru.controller.division.dto.DivisionUpdateReq;
import com.maru.domain.division.Division;
import com.maru.domain.division.exception.DivisionErrorCode;
import com.maru.domain.section.Section;
import com.maru.domain.section.exception.SectionErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.division.DivisionRepository;
import com.maru.repository.enrollment.EnrollmentRepository;
import com.maru.repository.enrollment.projection.StudentCountByDivision;
import com.maru.repository.section.SectionRepository;
import com.maru.repository.tenant.DojangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DivisionService {

    private final DivisionRepository divisionRepository;
    private final SectionRepository sectionRepository;
    private final DojangRepository dojangRepository;
    private final EnrollmentRepository enrollmentRepository;

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

        if (request.scheduleDays() != null && !request.scheduleDays().isEmpty()) {
            division.updateSchedule(request.scheduleDays(), request.startTime(), request.endTime());
        }

        divisionRepository.save(division);

        return DivisionRes.from(division, 0);
    }

    /**
     * 수련반 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID
     * @return 수련반 목록 (displayOrder 순)
     */
    public DivisionListRes getDivisions(String dojangId, String sectionId) {
        List<Division> divisions = divisionRepository.findAllWithSectionByDojangIdAndSectionId(dojangId, sectionId);

        Map<String, Integer> studentCountMap = getStudentCountMap(dojangId, divisions);

        List<DivisionRes> divisionResList = divisions.stream()
                .map(division -> DivisionRes.from(division, studentCountMap.getOrDefault(division.getId(), 0)))
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
        int studentCount = enrollmentRepository.countByDivisionId(dojangId, divisionId);
        return DivisionDetailRes.from(division, studentCount);
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
        division.updateSchedule(request.scheduleDays(), request.startTime(), request.endTime());

        int studentCount = enrollmentRepository.countByDivisionId(dojangId, divisionId);
        return DivisionRes.from(division, studentCount);
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

    /**
     * 수련반 순서 변경
     *
     * @param dojangId 도장 ID
     * @param request 순서 변경 요청 (수련부 ID, 수련반 ID 목록)
     * @throws BusinessException 중복 ID, 개수 불일치, 수련반 미존재 시
     */
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

    private Map<String, Integer> getStudentCountMap(String dojangId, List<Division> divisions) {
        if (divisions.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> divisionIds = divisions.stream()
                .map(Division::getId)
                .toList();
        return enrollmentRepository.countStudentsByDivisionIds(dojangId, divisionIds).stream()
                .collect(Collectors.toMap(
                        StudentCountByDivision::getDivisionId,
                        StudentCountByDivision::getStudentCount));
    }
}
