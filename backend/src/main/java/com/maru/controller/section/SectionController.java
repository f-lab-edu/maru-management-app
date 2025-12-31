package com.maru.controller.section;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.CommonErrorCode;
import com.maru.controller.section.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionController {

    @PostMapping
    public ResponseEntity<SectionRes> createSection(
            @RequestParam String dojangId,
            @Valid @RequestBody SectionCreateReq request
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    @GetMapping
    public ResponseEntity<SectionListRes> getSections(
            @RequestParam String dojangId
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    @PatchMapping("/{sectionId}")
    public ResponseEntity<SectionRes> updateSection(
            @RequestParam String dojangId,
            @PathVariable String sectionId,
            @Valid @RequestBody SectionUpdateReq request
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> deleteSection(
            @RequestParam String dojangId,
            @PathVariable String sectionId
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorderSections(
            @RequestParam String dojangId,
            @Valid @RequestBody SectionReorderReq request
    ) {
        throw new BusinessException(CommonErrorCode.NOT_IMPLEMENTED);
    }
}
