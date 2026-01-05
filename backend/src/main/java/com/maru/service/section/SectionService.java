package com.maru.service.section;

import com.maru.common.exception.BusinessException;
import com.maru.controller.section.dto.*;
import com.maru.domain.section.Section;
import com.maru.domain.section.exception.SectionErrorCode;
import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.exception.DojangErrorCode;
import com.maru.repository.division.DivisionRepository;
import com.maru.repository.division.view.DivisionCountBySectionView;
import com.maru.repository.section.SectionRepository;
import com.maru.repository.tenant.DojangRepository;
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
public class SectionService {

    private final SectionRepository sectionRepository;
    private final DojangRepository dojangRepository;
    private final DivisionRepository divisionRepository;

    /**
     * 수련부 생성
     *
     * @param dojangId 도장 ID
     * @param request  생성 요청
     * @return 생성된 수련부 정보
     * @throws BusinessException 중복 이름인 경우
     */
    @Transactional
    public SectionRes createSection(String dojangId, SectionCreateReq request) {
        Dojang dojang = findDojangById(dojangId);
        validateDuplicateName(dojangId, request.name());

        int nextDisplayOrder = sectionRepository.findMaxDisplayOrderByDojangId(dojangId) + 1;
        Section section = Section.create(dojang, request.name(), nextDisplayOrder);
        sectionRepository.save(section);

        return SectionRes.from(section, 0);
    }

    /**
     * 수련부 목록 조회
     *
     * @param dojangId 도장 ID
     * @return 수련부 목록 (displayOrder 순, groupCount 포함)
     */
    public SectionListRes getSections(String dojangId) {
        List<Section> sections = sectionRepository.findAllByDojangIdOrderByDisplayOrder(dojangId);
        Map<String, Integer> divisionCountMap = buildDivisionCountMap(dojangId);
        return toSectionListRes(sections, divisionCountMap);
    }

    private Map<String, Integer> buildDivisionCountMap(String dojangId) {
        return divisionRepository.countDivisionsBySectionForDojang(dojangId).stream()
                .collect(Collectors.toMap(
                        DivisionCountBySectionView::getSectionId,
                        DivisionCountBySectionView::getDivisionCount
                ));
    }

    /**
     * 수련부 수정
     *
     * @param dojangId  도장 ID
     * @param sectionId 수련부 ID
     * @param request   수정 요청
     * @return 수정된 수련부 정보
     * @throws BusinessException 수련부를 찾을 수 없거나 중복 이름인 경우
     */
    @Transactional
    public SectionRes updateSection(String dojangId, String sectionId, SectionUpdateReq request) {
        Section section = findSectionByIdAndDojangId(sectionId, dojangId);
        validateDuplicateNameExcludingSelf(dojangId, request.name(), sectionId);

        section.updateName(request.name());

        return SectionRes.from(section, 0);
    }

    /**
     * 수련부 삭제
     *
     * @param dojangId  도장 ID
     * @param sectionId 수련부 ID
     * @throws BusinessException 수련부를 찾을 수 없거나 소속 수련반이 있는 경우
     */
    @Transactional
    public void deleteSection(String dojangId, String sectionId) {
        Section section = findSectionByIdAndDojangId(sectionId, dojangId);
        validateNoDivisions(sectionId);
        sectionRepository.delete(section);
    }

    private void validateNoDivisions(String sectionId) {
        if (divisionRepository.existsBySectionId(sectionId)) {
            throw new BusinessException(SectionErrorCode.HAS_CLASSES);
        }
    }

    /**
     * 수련부 순서 변경
     *
     * @param dojangId 도장 ID
     * @param request  순서 변경 요청 (ID 목록 순서대로 displayOrder 부여)
     * @throws BusinessException 요청한 수련부 갯수와 실제 갯수가 다른 경우
     */
    @Transactional
    public void reorderSections(String dojangId, SectionReorderReq request) {
        List<String> sectionIds = request.sectionIds();

        validateNoDuplicateIds(sectionIds);
        List<Section> allSections = findAllSectionsAndValidateCount(dojangId, sectionIds);
        updateDisplayOrders(allSections, sectionIds);
    }

    private Dojang findDojangById(String dojangId) {
        return dojangRepository.findById(dojangId)
                .orElseThrow(() -> new BusinessException(DojangErrorCode.NOT_FOUND));
    }

    private Section findSectionByIdAndDojangId(String sectionId, String dojangId) {
        return sectionRepository.findByIdAndDojangId(sectionId, dojangId)
                .orElseThrow(() -> new BusinessException(SectionErrorCode.NOT_FOUND));
    }

    private SectionListRes toSectionListRes(List<Section> sections, Map<String, Integer> groupCountMap) {
        List<SectionRes> sectionResList = sections.stream()
                .map(section -> SectionRes.from(section, groupCountMap.getOrDefault(section.getId(), 0)))
                .toList();
        return SectionListRes.from(sectionResList);
    }

    private void validateDuplicateName(String dojangId, String name) {
        if (sectionRepository.existsByDojangIdAndName(dojangId, name)) {
            throw new BusinessException(SectionErrorCode.DUPLICATE_NAME);
        }
    }

    private void validateDuplicateNameExcludingSelf(String dojangId, String name, String excludeId) {
        if (sectionRepository.existsByDojangIdAndNameAndIdNot(dojangId, name, excludeId)) {
            throw new BusinessException(SectionErrorCode.DUPLICATE_NAME);
        }
    }

    private void validateNoDuplicateIds(List<String> sectionIds) {
        Set<String> sectionIdSet = new HashSet<>(sectionIds);
        if (sectionIdSet.size() != sectionIds.size()) {
            throw new BusinessException(SectionErrorCode.DUPLICATE_ID_IN_REQUEST);
        }
    }

    private List<Section> findAllSectionsAndValidateCount(String dojangId, List<String> sectionIds) {
        List<Section> allSections = sectionRepository.findAllByDojangIdOrderByDisplayOrder(dojangId);
        if (allSections.size() != sectionIds.size()) {
            throw new BusinessException(SectionErrorCode.REORDER_COUNT_MISMATCH);
        }
        return allSections;
    }

    private void updateDisplayOrders(List<Section> allSections, List<String> sectionIds) {
        Map<String, Section> sectionMap = allSections.stream()
                .collect(Collectors.toMap(Section::getId, Function.identity()));

        for (int i = 0; i < sectionIds.size(); i++) {
            Section section = sectionMap.get(sectionIds.get(i));
            if (section == null) {
                throw new BusinessException(SectionErrorCode.NOT_FOUND);
            }
            section.updateDisplayOrder(i);
        }
    }
}
