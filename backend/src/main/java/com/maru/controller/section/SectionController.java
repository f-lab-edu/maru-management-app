package com.maru.controller.section;

import com.maru.controller.section.dto.*;
import com.maru.service.section.SectionQueryService;
import com.maru.service.section.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final SectionQueryService sectionQueryService;

    /**
     * 수련부 생성
     *
     * @param dojangId 도장 ID
     * @param request 수련부 생성 정보
     * @return 생성된 수련부 정보
     */
    @PostMapping
    public ResponseEntity<SectionRes> createSection(
            @RequestParam String dojangId,
            @Valid @RequestBody SectionCreateReq request
    ) {
        SectionRes response = sectionService.createSection(dojangId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 수련부 목록 조회
     *
     * @param dojangId 도장 ID
     * @return 수련부 목록 (displayOrder 순)
     */
    @GetMapping
    public ResponseEntity<SectionListRes> getSections(
            @RequestParam String dojangId
    ) {
        SectionListRes response = sectionQueryService.getSections(dojangId);
        return ResponseEntity.ok(response);
    }

    /**
     * 수련부 수정
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID
     * @param request 수정 정보
     * @return 수정된 수련부 정보
     */
    @PatchMapping("/{sectionId}")
    public ResponseEntity<SectionRes> updateSection(
            @RequestParam String dojangId,
            @PathVariable String sectionId,
            @Valid @RequestBody SectionUpdateReq request
    ) {
        SectionRes response = sectionService.updateSection(dojangId, sectionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 수련부 삭제
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> deleteSection(
            @RequestParam String dojangId,
            @PathVariable String sectionId
    ) {
        sectionService.deleteSection(dojangId, sectionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 수련부 순서 변경
     *
     * @param dojangId 도장 ID
     * @param request 순서 변경 요청 (ID 목록 순서대로 displayOrder 부여)
     * @return 204 No Content
     */
    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorderSections(
            @RequestParam String dojangId,
            @Valid @RequestBody SectionReorderReq request
    ) {
        sectionService.reorderSections(dojangId, request);
        return ResponseEntity.noContent().build();
    }
}
