package com.maru.service.message;

import com.maru.common.exception.BusinessException;
import com.maru.controller.message.dto.AttemptRes;
import com.maru.controller.message.dto.BroadcastDetailRes;
import com.maru.controller.message.dto.BroadcastSummaryRes;
import com.maru.controller.message.dto.MessageDetailRes;
import com.maru.controller.message.dto.MessageSummaryRes;
import com.maru.controller.message.dto.NotificationDailySummaryRes;
import com.maru.controller.message.dto.NotificationDetailRes;
import com.maru.controller.message.dto.RecipientStatusRes;
import com.maru.domain.message.MessageBroadcast;
import com.maru.domain.message.MessageBroadcastStatus;
import com.maru.domain.message.MessageDispatch;
import com.maru.domain.message.MessageStatus;
import com.maru.domain.message.MessageType;
import com.maru.domain.message.exception.MessageBroadcastErrorCode;
import com.maru.domain.message.exception.MessageDispatchErrorCode;
import com.maru.domain.user.User;
import com.maru.repository.guardian.view.GuardianBasicView;
import com.maru.repository.guardian.view.GuardianStudentNameView;
import com.maru.repository.guardian.GuardianRepository;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.message.MessageBroadcastRepository;
import com.maru.repository.user.UserRepository;
import com.maru.repository.message.MessageDispatchAttemptRepository;
import com.maru.repository.message.MessageDispatchRepository;
import com.maru.repository.message.view.BroadcastStatusCountView;
import com.maru.repository.message.view.NotificationSummaryView;
import com.maru.repository.message.view.StatusCountView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageQueryService {

    private static final Map<String, String> MESSAGE_TYPE_LABELS = Map.of(
            MessageType.ATTENDANCE_CHECKIN.name(), "출결 알림 (입실)",
            MessageType.ATTENDANCE_CHECKOUT.name(), "출결 알림 (퇴원)",
            MessageType.PAYMENT.name(), "결제 링크"
    );

    private final MessageDispatchRepository messageDispatchRepository;
    private final MessageDispatchAttemptRepository attemptRepository;
    private final MessageBroadcastRepository broadcastRepository;
    private final GuardianRepository guardianRepository;
    private final GuardianshipRepository guardianshipRepository;
    private final UserRepository userRepository;

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

        return attemptRepository.findByDispatchOrderByAttemptNumberAsc(message).stream()
                .map(AttemptRes::from)
                .toList();
    }

    /**
     * 단체 발송 목록 조회
     *
     * @param dojangId 도장 ID
     * @param pageable 페이징
     * @return 단체 발송 요약 목록
     */
    public Page<BroadcastSummaryRes> findBroadcasts(String dojangId, Pageable pageable) {
        Page<MessageBroadcast> broadcasts = broadcastRepository.findByDojangIdOrderByCreatedAtDesc(dojangId, pageable);

        List<String> broadcastIds = broadcasts.getContent().stream()
                .map(MessageBroadcast::getId)
                .toList();
        Map<String, StatusCounts> countsMap = batchAggregateStatusCounts(broadcastIds);

        return broadcasts.map(broadcast -> {
            StatusCounts counts = countsMap.getOrDefault(broadcast.getId(), StatusCounts.EMPTY);
            return toBroadcastSummary(broadcast, counts);
        });
    }

    /**
     * 단체 발송 상세 조회
     *
     * @param dojangId 도장 ID
     * @param broadcastId 발송 ID
     * @return 단체 발송 상세 정보
     * @throws BusinessException NOT_FOUND - 발송을 찾을 수 없거나 도장이 다른 경우
     */
    public BroadcastDetailRes findBroadcastDetail(String dojangId, String broadcastId) {
        MessageBroadcast broadcast = broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new BusinessException(MessageBroadcastErrorCode.NOT_FOUND));

        if (!broadcast.getDojangId().equals(dojangId)) {
            throw new BusinessException(MessageBroadcastErrorCode.NOT_FOUND);
        }

        StatusCounts counts = aggregateStatusCounts(broadcastId);
        String sentByUserName = userRepository.findById(broadcast.getSentByUserId())
                .map(User::getName)
                .orElse(null);

        return toBroadcastDetail(broadcast, counts, sentByUserName);
    }

    /**
     * 단체 발송 수신자 상태 조회
     *
     * @param dojangId 도장 ID
     * @param broadcastId 발송 ID
     * @param status 상태 필터 (null이면 전체)
     * @param pageable 페이징
     * @return 수신자별 발송 상태 목록
     */
    public Page<RecipientStatusRes> findBroadcastRecipients(
            String dojangId, String broadcastId, MessageStatus status, Pageable pageable) {

        Page<MessageDispatch> dispatches = status != null
                ? messageDispatchRepository.findByBroadcastIdAndStatus(broadcastId, status, pageable)
                : messageDispatchRepository.findByBroadcastId(broadcastId, pageable);

        List<String> guardianIds = extractGuardianIds(dispatches);
        Map<String, GuardianBasicView> guardianMap = buildGuardianMap(guardianIds);
        Map<String, String> studentNameMap = guardianIds.isEmpty()
                ? Map.of()
                : buildGuardianStudentNameMap(guardianIds);

        return dispatches.map(dispatch -> toRecipientStatus(dispatch, guardianMap, studentNameMap));
    }

    /**
     * 자동 발송 일별 요약 조회
     *
     * @param dojangId 도장 ID
     * @param pageable 페이징
     * @return 일별 발송 요약 목록
     */
    public Page<NotificationDailySummaryRes> findNotificationSummary(String dojangId, Pageable pageable) {
        Page<NotificationSummaryView> views = messageDispatchRepository.findNotificationSummary(dojangId, pageable);
        return views.map(this::toNotificationDailySummary);
    }

    /**
     * 자동 발송 상세 목록 조회
     *
     * @param dojangId 도장 ID
     * @param date 발송 날짜
     * @param messageType 메시지 타입
     * @param pageable 페이징
     * @return 발송 상세 목록
     */
    public Page<NotificationDetailRes> findNotificationDetails(
            String dojangId, LocalDate date, String messageType, Pageable pageable) {

        Page<MessageDispatch> dispatches = messageDispatchRepository.findNotificationDetails(
                dojangId, messageType, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), pageable);

        List<String> guardianIds = extractGuardianIds(dispatches);
        Map<String, GuardianBasicView> guardianMap = buildGuardianMap(guardianIds);

        return dispatches.map(dispatch -> toNotificationDetail(dispatch, guardianMap));
    }

    private BroadcastSummaryRes toBroadcastSummary(MessageBroadcast broadcast, StatusCounts counts) {
        return BroadcastSummaryRes.builder()
                .id(broadcast.getId())
                .title(broadcast.getTitle())
                .channel(broadcast.getChannel())
                .recipientType(broadcast.getRecipientType())
                .totalCount(broadcast.getTotalCount())
                .skippedCount(broadcast.getSkippedCount())
                .acceptedCount(counts.accepted())
                .failedCount(counts.failed())
                .pendingCount(counts.pending() + counts.processing())
                .status(determineBroadcastStatus(counts).name())
                .createdAt(broadcast.getCreatedAt())
                .build();
    }

    private BroadcastDetailRes toBroadcastDetail(
            MessageBroadcast broadcast, StatusCounts counts, String sentByUserName) {
        return BroadcastDetailRes.builder()
                .id(broadcast.getId())
                .title(broadcast.getTitle())
                .body(broadcast.getBody())
                .channel(broadcast.getChannel())
                .recipientType(broadcast.getRecipientType())
                .totalCount(broadcast.getTotalCount())
                .skippedCount(broadcast.getSkippedCount())
                .acceptedCount(counts.accepted())
                .failedCount(counts.failed())
                .pendingCount(counts.pending() + counts.processing())
                .status(determineBroadcastStatus(counts).name())
                .sentByUserName(sentByUserName)
                .createdAt(broadcast.getCreatedAt())
                .build();
    }

    private RecipientStatusRes toRecipientStatus(
            MessageDispatch dispatch,
            Map<String, GuardianBasicView> guardianMap,
            Map<String, String> studentNameMap) {
        GuardianBasicView guardian = guardianMap.get(dispatch.getGuardianId());
        return RecipientStatusRes.builder()
                .guardianId(dispatch.getGuardianId())
                .guardianName(guardian != null ? guardian.getName() : null)
                .phone(guardian != null ? guardian.getPhone() : null)
                .studentName(studentNameMap.get(dispatch.getGuardianId()))
                .messageStatus(dispatch.getStatus().name())
                .sentAt(dispatch.getSentAt())
                .build();
    }

    private NotificationDailySummaryRes toNotificationDailySummary(NotificationSummaryView view) {
        return NotificationDailySummaryRes.builder()
                .sendDate(view.getSendDate())
                .messageType(view.getMessageType())
                .messageTypeLabel(MESSAGE_TYPE_LABELS.getOrDefault(view.getMessageType(), view.getMessageType()))
                .totalCount(view.getTotalCount())
                .acceptedCount(view.getAcceptedCount())
                .failedCount(view.getFailedCount())
                .build();
    }

    private NotificationDetailRes toNotificationDetail(
            MessageDispatch dispatch, Map<String, GuardianBasicView> guardianMap) {
        GuardianBasicView guardian = guardianMap.get(dispatch.getGuardianId());
        return NotificationDetailRes.builder()
                .id(dispatch.getId())
                .guardianName(guardian != null ? guardian.getName() : null)
                .title(dispatch.getTitle())
                .status(dispatch.getStatus().name())
                .createdAt(dispatch.getCreatedAt())
                .sentAt(dispatch.getSentAt())
                .build();
    }

    private List<String> extractGuardianIds(Page<MessageDispatch> dispatches) {
        return dispatches.getContent().stream()
                .map(MessageDispatch::getGuardianId)
                .distinct()
                .toList();
    }

    private Map<String, GuardianBasicView> buildGuardianMap(List<String> guardianIds) {
        if (guardianIds.isEmpty()) {
            return Map.of();
        }
        return guardianRepository.findBasicInfoByIds(guardianIds).stream()
                .collect(Collectors.toMap(GuardianBasicView::getId, v -> v, (a, b) -> a));
    }

    private StatusCounts aggregateStatusCounts(String broadcastId) {
        List<StatusCountView> views = messageDispatchRepository.countByBroadcastIdGroupByStatus(broadcastId);

        long pending = 0, processing = 0, accepted = 0, failed = 0;
        for (StatusCountView v : views) {
            switch (MessageStatus.valueOf(v.getStatus())) {
                case PENDING -> pending = v.getCount();
                case PROCESSING -> processing = v.getCount();
                case ACCEPTED -> accepted = v.getCount();
                case DEAD -> failed = v.getCount();
            }
        }
        return new StatusCounts(pending, processing, accepted, failed);
    }

    private MessageBroadcastStatus determineBroadcastStatus(StatusCounts counts) {
        if (counts.pending() == 0 && counts.processing() == 0) {
            return counts.failed() > 0 ? MessageBroadcastStatus.PARTIAL_FAILED : MessageBroadcastStatus.COMPLETED;
        }
        return MessageBroadcastStatus.DISPATCHING;
    }

    private record StatusCounts(long pending, long processing, long accepted, long failed) {
        static final StatusCounts EMPTY = new StatusCounts(0, 0, 0, 0);
    }

    private Map<String, StatusCounts> batchAggregateStatusCounts(List<String> broadcastIds) {
        if (broadcastIds.isEmpty()) {
            return Map.of();
        }
        List<BroadcastStatusCountView> views =
                messageDispatchRepository.countByBroadcastIdsGroupByStatus(broadcastIds);

        Map<String, long[]> tempMap = new HashMap<>();
        for (BroadcastStatusCountView v : views) {
            long[] arr = tempMap.computeIfAbsent(v.getBroadcastId(), k -> new long[4]);
            switch (MessageStatus.valueOf(v.getStatus())) {
                case PENDING -> arr[0] = v.getCount();
                case PROCESSING -> arr[1] = v.getCount();
                case ACCEPTED -> arr[2] = v.getCount();
                case DEAD -> arr[3] = v.getCount();
            }
        }

        Map<String, StatusCounts> result = new HashMap<>();
        tempMap.forEach((id, arr) -> result.put(id, new StatusCounts(arr[0], arr[1], arr[2], arr[3])));
        return result;
    }

    private Map<String, String> buildGuardianStudentNameMap(List<String> guardianIds) {
        var views = guardianshipRepository.findStudentNamesByGuardianIds(guardianIds);
        Map<String, List<String>> grouped = views.stream()
                .collect(Collectors.groupingBy(
                        GuardianStudentNameView::getGuardianId,
                        Collectors.mapping(GuardianStudentNameView::getStudentName, Collectors.toList())));

        Map<String, String> result = new HashMap<>();
        grouped.forEach((guardianId, names) -> {
            List<String> distinct = names.stream().distinct().toList();
            if (distinct.size() == 1) {
                result.put(guardianId, distinct.getFirst());
            } else {
                result.put(guardianId, distinct.getFirst() + " 외 " + (distinct.size() - 1) + "명");
            }
        });
        return result;
    }
}
