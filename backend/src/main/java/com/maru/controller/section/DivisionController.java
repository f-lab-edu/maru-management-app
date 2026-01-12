package com.maru.controller.section;

import com.maru.controller.section.dto.DivisionCreateReq;
import com.maru.controller.section.dto.DivisionDetailRes;
import com.maru.controller.section.dto.DivisionListRes;
import com.maru.controller.section.dto.DivisionReorderReq;
import com.maru.controller.section.dto.DivisionRes;
import com.maru.controller.section.dto.DivisionUpdateReq;
import com.maru.service.section.DivisionQueryService;
import com.maru.service.section.DivisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/divisions")
@RequiredArgsConstructor
public class DivisionController {

    private final DivisionService divisionService;
    private final DivisionQueryService divisionQueryService;

    /**
     * 수련반 생성
     *
     * @param dojangId 도장 ID
     * @param request 수련반 생성 정보
     * @return 생성된 수련반 정보
     */
    @PostMapping
    public ResponseEntity<DivisionRes> createDivision(
            @RequestParam String dojangId,
            @Valid @RequestBody DivisionCreateReq request
    ) {
        DivisionRes response = divisionService.createDivision(dojangId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 수련반 목록 조회
     *
     * @param dojangId 도장 ID
     * @param sectionId 수련부 ID
     * @return 수련반 목록 (displayOrder 순)
     */
    @GetMapping
    public ResponseEntity<DivisionListRes> getDivisions(
            @RequestParam String dojangId,
            @RequestParam String sectionId
    ) {
        DivisionListRes response = divisionQueryService.getDivisions(dojangId, sectionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 수련반 상세 조회
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 수련반 상세 정보
     */
    @GetMapping("/{divisionId}")
    public ResponseEntity<DivisionDetailRes> getDivisionDetail(
            @RequestParam String dojangId,
            @PathVariable String divisionId
    ) {
        DivisionDetailRes response = divisionQueryService.getDivisionDetail(dojangId, divisionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 수련반 수정
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @param request 수정 정보
     * @return 수정된 수련반 정보
     */
    @PatchMapping("/{divisionId}")
    public ResponseEntity<DivisionRes> updateDivision(
            @RequestParam String dojangId,
            @PathVariable String divisionId,
            @Valid @RequestBody DivisionUpdateReq request
    ) {
        DivisionRes response = divisionService.updateDivision(dojangId, divisionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 수련반 삭제
     *
     * @param dojangId 도장 ID
     * @param divisionId 수련반 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{divisionId}")
    public ResponseEntity<Void> deleteDivision(
            @RequestParam String dojangId,
            @PathVariable String divisionId
    ) {
        divisionService.deleteDivision(dojangId, divisionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 수련반 순서 변경
     *
     * @param dojangId 도장 ID
     * @param request 순서 변경 요청 (수련부 ID, 수련반 ID 목록)
     * @return 204 No Content
     */
    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorderDivisions(
            @RequestParam String dojangId,
            @Valid @RequestBody DivisionReorderReq request
    ) {
        divisionService.reorderDivisions(dojangId, request);
        return ResponseEntity.noContent().build();
    }
}
