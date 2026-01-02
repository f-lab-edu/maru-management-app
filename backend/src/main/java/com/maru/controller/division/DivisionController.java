package com.maru.controller.division;

import com.maru.controller.division.dto.DivisionCreateReq;
import com.maru.controller.division.dto.DivisionDetailRes;
import com.maru.controller.division.dto.DivisionListRes;
import com.maru.controller.division.dto.DivisionRes;
import com.maru.controller.division.dto.DivisionUpdateReq;
import com.maru.service.division.DivisionService;
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
     * @param sectionId 수련부 ID (선택, null이면 전체)
     * @return 수련반 목록 (section.displayOrder, displayOrder 순)
     */
    @GetMapping
    public ResponseEntity<DivisionListRes> getDivisions(
            @RequestParam String dojangId,
            @RequestParam(required = false) String sectionId
    ) {
        DivisionListRes response = divisionService.getDivisions(dojangId, sectionId);
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
        DivisionDetailRes response = divisionService.getDivisionDetail(dojangId, divisionId);
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
}
