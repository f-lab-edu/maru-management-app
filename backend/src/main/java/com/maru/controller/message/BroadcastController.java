package com.maru.controller.message;

import com.maru.controller.message.dto.BroadcastCreateReq;
import com.maru.controller.message.dto.BroadcastCreateRes;
import com.maru.controller.message.dto.BroadcastDetailRes;
import com.maru.controller.message.dto.BroadcastSummaryRes;
import com.maru.controller.message.dto.RecipientPreviewReq;
import com.maru.controller.message.dto.RecipientPreviewRes;
import com.maru.controller.message.dto.RecipientStatusRes;
import com.maru.domain.message.MessageStatus;
import com.maru.security.CurrentUserId;
import com.maru.service.message.MessageBroadcastService;
import com.maru.service.message.MessageQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "단체 문자")
@RestController
@RequestMapping("/api/v1/broadcasts")
@RequiredArgsConstructor
public class BroadcastController {

    private final MessageBroadcastService broadcastService;
    private final MessageQueryService queryService;

    /**
     * 단체 문자 발송 생성
     *
     * @param dojangId 도장 ID
     * @param userId 발송 요청 사용자 ID
     * @param req 발송 요청 정보
     * @return 생성된 발송 정보
     */
    @PostMapping
    public ResponseEntity<BroadcastCreateRes> createBroadcast(
            @RequestParam String dojangId,
            @CurrentUserId String userId,
            @Valid @RequestBody BroadcastCreateReq req) {
        BroadcastCreateRes res = broadcastService.createBroadcast(dojangId, userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * 단체 발송 목록 조회
     *
     * @param dojangId 도장 ID
     * @param userId 사용자 ID
     * @param pageable 페이징
     * @return 단체 발송 목록
     */
    @GetMapping
    public ResponseEntity<Page<BroadcastSummaryRes>> getBroadcasts(
            @RequestParam String dojangId,
            @CurrentUserId String userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(queryService.findBroadcasts(dojangId, pageable));
    }

    /**
     * 단체 발송 상세 조회
     *
     * @param dojangId 도장 ID
     * @param userId 사용자 ID
     * @param broadcastId 발송 ID
     * @return 단체 발송 상세 정보
     */
    @GetMapping("/{broadcastId}")
    public ResponseEntity<BroadcastDetailRes> getBroadcastDetail(
            @RequestParam String dojangId,
            @CurrentUserId String userId,
            @PathVariable String broadcastId) {
        return ResponseEntity.ok(queryService.findBroadcastDetail(dojangId, broadcastId));
    }

    /**
     * 단체 발송 수신자 목록 조회
     *
     * @param dojangId 도장 ID
     * @param userId 사용자 ID
     * @param broadcastId 발송 ID
     * @param status 상태 필터 (null이면 전체)
     * @param pageable 페이징
     * @return 수신자 상태 목록
     */
    @GetMapping("/{broadcastId}/recipients")
    public ResponseEntity<Page<RecipientStatusRes>> getRecipients(
            @RequestParam String dojangId,
            @CurrentUserId String userId,
            @PathVariable String broadcastId,
            @RequestParam(required = false) MessageStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(queryService.findBroadcastRecipients(dojangId, broadcastId, status, pageable));
    }

    /**
     * 수신 대상 미리보기
     *
     * @param dojangId 도장 ID
     * @param userId 사용자 ID
     * @param req 수신자 조건
     * @return 예상 수신자 수 및 제외 정보
     */
    @PostMapping("/preview-recipients")
    public ResponseEntity<RecipientPreviewRes> previewRecipients(
            @RequestParam String dojangId,
            @CurrentUserId String userId,
            @Valid @RequestBody RecipientPreviewReq req) {
        return ResponseEntity.ok(broadcastService.previewRecipients(dojangId, req));
    }
}
