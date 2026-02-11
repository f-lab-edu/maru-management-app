package com.maru.controller.invoice;

import com.maru.controller.invoice.dto.SubMerchantCreateReq;
import com.maru.controller.invoice.dto.SubMerchantRes;
import com.maru.controller.invoice.dto.SubMerchantStatusUpdateReq;
import com.maru.security.CurrentUserId;
import com.maru.service.invoice.SubMerchantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "서브몰")
@RestController
@RequestMapping("/api/v1/sub-merchants")
@RequiredArgsConstructor
public class SubMerchantController {

    private final SubMerchantService subMerchantService;

    /**
     * 서브몰 등록
     *
     * @param dojangId 도장 ID
     * @param request 서브몰 등록 요청
     * @param userId 현재 인증된 사용자 ID
     * @return 등록된 서브몰 정보
     */
    @PostMapping
    public ResponseEntity<SubMerchantRes> register(
            @RequestParam String dojangId,
            @Valid @RequestBody SubMerchantCreateReq request,
            @CurrentUserId String userId) {
        SubMerchantRes response = subMerchantService.register(dojangId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 서브몰 조회
     *
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 서브몰 정보
     */
    @GetMapping("/{dojangId}")
    public ResponseEntity<SubMerchantRes> getByDojangId(
            @PathVariable String dojangId,
            @CurrentUserId String userId) {
        SubMerchantRes response = subMerchantService.getByDojangId(dojangId);
        return ResponseEntity.ok(response);
    }

    /**
     * 서브몰 상태 변경
     *
     * @param dojangId 도장 ID
     * @param request 상태 변경 요청
     * @param userId 현재 인증된 사용자 ID
     * @return 변경된 서브몰 정보
     */
    @PatchMapping("/{dojangId}/status")
    public ResponseEntity<SubMerchantRes> updateStatus(
            @PathVariable String dojangId,
            @Valid @RequestBody SubMerchantStatusUpdateReq request,
            @CurrentUserId String userId) {
        SubMerchantRes response = subMerchantService.updateStatus(dojangId, request);
        return ResponseEntity.ok(response);
    }
}
