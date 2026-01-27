package com.maru.controller.message;

import com.maru.controller.message.dto.AttemptRes;
import com.maru.controller.message.dto.MessageDetailRes;
import com.maru.controller.message.dto.MessageSummaryRes;
import com.maru.domain.message.MessageStatus;
import com.maru.security.CurrentUserId;
import com.maru.service.message.MessageQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "메시지")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageQueryService messageQueryService;

    /**
     * 내 메시지 목록 조회
     *
     * @param userId 현재 인증된 사용자 ID (보호자 ID)
     * @param status 상태 필터 (선택)
     * @param pageable 페이징
     * @return 메시지 목록
     */
    @GetMapping
    public ResponseEntity<Page<MessageSummaryRes>> getMyMessages(
            @CurrentUserId String userId,
            @RequestParam(required = false) MessageStatus status,
            Pageable pageable
    ) {
        Page<MessageSummaryRes> messages = messageQueryService.findByGuardian(userId, status, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * 메시지 상세 조회
     *
     * @param userId 현재 인증된 사용자 ID (보호자 ID)
     * @param messageId 메시지 ID
     * @return 메시지 상세
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageDetailRes> getMessageDetail(
            @CurrentUserId String userId,
            @PathVariable String messageId
    ) {
        MessageDetailRes detail = messageQueryService.findDetail(userId, messageId);
        return ResponseEntity.ok(detail);
    }

    /**
     * 메시지 발송 시도 히스토리 조회
     *
     * @param userId 현재 인증된 사용자 ID (보호자 ID)
     * @param messageId 메시지 ID
     * @return 발송 시도 목록
     */
    @GetMapping("/{messageId}/attempts")
    public ResponseEntity<List<AttemptRes>> getAttempts(
            @CurrentUserId String userId,
            @PathVariable String messageId
    ) {
        List<AttemptRes> attempts = messageQueryService.findAttempts(userId, messageId);
        return ResponseEntity.ok(attempts);
    }
}
