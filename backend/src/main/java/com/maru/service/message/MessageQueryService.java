package com.maru.service.message;

import com.maru.common.exception.BusinessException;
import com.maru.controller.message.dto.AttemptRes;
import com.maru.controller.message.dto.MessageDetailRes;
import com.maru.controller.message.dto.MessageSummaryRes;
import com.maru.domain.message.MessageDispatch;
import com.maru.domain.message.MessageStatus;
import com.maru.domain.message.exception.MessageDispatchErrorCode;
import com.maru.repository.message.MessageDispatchAttemptRepository;
import com.maru.repository.message.MessageDispatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageQueryService {

    private final MessageDispatchRepository messageDispatchRepository;
    private final MessageDispatchAttemptRepository attemptRepository;

    /**
     * 보호자별 메시지 목록 조회
     *
     * @param guardianId 보호자 ID
     * @param status 상태 필터 (null이면 전체)
     * @param pageable 페이징
     * @return 메시지 목록
     */
    public Page<MessageSummaryRes> findByGuardian(String guardianId, MessageStatus status, Pageable pageable) {
        Page<MessageDispatch> messages = status != null
                ? messageDispatchRepository.findByGuardianIdAndStatus(guardianId, status, pageable)
                : messageDispatchRepository.findByGuardianId(guardianId, pageable);

        return messages.map(MessageSummaryRes::from);
    }

    /**
     * 메시지 상세 조회
     *
     * @param guardianId 보호자 ID
     * @param messageId 메시지 ID
     * @return 메시지 상세
     * @throws BusinessException 메시지를 찾을 수 없거나 권한이 없는 경우
     */
    public MessageDetailRes findDetail(String guardianId, String messageId) {
        MessageDispatch message = messageDispatchRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(MessageDispatchErrorCode.NOT_FOUND));

        if (!message.getGuardianId().equals(guardianId)) {
            throw new BusinessException(MessageDispatchErrorCode.NOT_FOUND);
        }

        return MessageDetailRes.from(message);
    }

    /**
     * 메시지 발송 시도 히스토리 조회
     *
     * @param guardianId 보호자 ID
     * @param messageId 메시지 ID
     * @return 발송 시도 목록
     * @throws BusinessException 메시지를 찾을 수 없거나 권한이 없는 경우
     */
    public List<AttemptRes> findAttempts(String guardianId, String messageId) {
        MessageDispatch message = messageDispatchRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(MessageDispatchErrorCode.NOT_FOUND));

        if (!message.getGuardianId().equals(guardianId)) {
            throw new BusinessException(MessageDispatchErrorCode.NOT_FOUND);
        }

        return attemptRepository.findByDispatchIdOrderByAttemptNumberAsc(message).stream()
                .map(AttemptRes::from)
                .toList();
    }
}
