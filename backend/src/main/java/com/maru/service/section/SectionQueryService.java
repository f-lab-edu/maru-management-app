package com.maru.service.section;

import com.maru.controller.section.dto.SectionListRes;
import com.maru.controller.section.dto.SectionRes;
import com.maru.repository.section.SectionRepository;
import com.maru.repository.section.view.SectionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SectionQueryService {

    private final SectionRepository sectionRepository;

    /**
     * 수련부 목록 조회
     *
     * @param dojangId 도장 ID
     * @return 수련부 목록 (displayOrder 순, divisionCount 포함)
     */
    public SectionListRes getSections(String dojangId) {
        List<SectionView> views = sectionRepository.findAllWithDivisionCount(dojangId);
        List<SectionRes> sections = views.stream()
                .map(this::toSectionRes)
                .toList();
        return SectionListRes.from(sections);
    }

    private SectionRes toSectionRes(SectionView view) {
        return SectionRes.builder()
                .id(view.getId())
                .name(view.getName())
                .displayOrder(view.getDisplayOrder())
                .divisionCount(view.getDivisionCount())
                .build();
    }
}
