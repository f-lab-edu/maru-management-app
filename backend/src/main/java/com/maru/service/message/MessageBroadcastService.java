package com.maru.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maru.common.exception.BusinessException;
import com.maru.controller.message.dto.BroadcastCreateReq;
import com.maru.controller.message.dto.BroadcastCreateRes;
import com.maru.controller.message.dto.RecipientPreviewReq;
import com.maru.controller.message.dto.RecipientPreviewRes;
import com.maru.controller.message.dto.ResendFailedRes;
import com.maru.domain.message.MessageBroadcast;
import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.MessageDispatch;
import com.maru.domain.message.MessageStatus;
import com.maru.domain.message.MessageType;
import com.maru.domain.message.RecipientType;
import com.maru.domain.message.exception.MessageBroadcastErrorCode;
import com.maru.domain.student.StudentStatus;
import com.maru.repository.enrollment.EnrollmentRepository;
import com.maru.repository.guardian.view.BroadcastRecipientView;
import com.maru.repository.guardian.GuardianshipRepository;
import com.maru.repository.message.MessageBroadcastRepository;
import com.maru.repository.message.MessageDispatchRepository;
import com.maru.repository.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.maru.common.aop.ValidateDojangAccess;
import com.maru.security.TenantContextHolder;

@Slf4j
@Service
@RequiredArgsConstructor
@ValidateDojangAccess
public class MessageBroadcastService {

    private static final String REF_TYPE_BROADCAST = "BROADCAST";

    private final MessageBroadcastRepository broadcastRepository;
    private final MessageDispatchRepository dispatchRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GuardianshipRepository guardianshipRepository;
    private final ObjectMapper objectMapper;

    /**
     * 단체 문자 발송 생성 및 MessageDispatch 생성
     *
     * @param dojangId 도장 ID
     * @param userId 발송 요청 사용자 ID
     * @param req 발송 요청 정보
     * @return 생성된 발송 정보
     * @throws BusinessException NO_RECIPIENTS - 수신 가능한 보호자가 없는 경우
     */
    @Transactional
    public BroadcastCreateRes createBroadcast(String dojangId, String userId, BroadcastCreateReq req) {
        String tenantId = TenantContextHolder.getTenantId();

        RecipientResolution resolution = resolveRecipients(tenantId, dojangId, req.recipientType(),
                req.sectionIds(), req.divisionIds(), req.studentIds());

        if (resolution.recipients().isEmpty()) {
            throw new BusinessException(MessageBroadcastErrorCode.NO_RECIPIENTS);
        }

        MessageChannel channel = req.channelOrDefault();
        String criteriaJson = serializeCriteria(req);

        MessageBroadcast broadcast = MessageBroadcast.create(
                tenantId, dojangId, userId,
                req.title(), req.body(), channel,
                req.recipientType(), criteriaJson,
                resolution.recipients().size(),
                resolution.skippedNoGuardian() + resolution.skippedNoPhone());
        broadcastRepository.save(broadcast);

        List<MessageDispatch> dispatches = resolution.recipients().stream()
                .map(r -> MessageDispatch.createPending(
                        tenantId, dojangId, r.guardianId(),
                        REF_TYPE_BROADCAST, broadcast.getId(),
                        MessageType.ANNOUNCEMENT, channel,
                        req.title(), req.body(), null))
                .toList();
        dispatchRepository.saveAll(dispatches);

        broadcast.startDispatching();

        log.info("단체 문자 발송 생성: broadcastId={}, 수신자={}명, 건너뜀={}명",
                broadcast.getId(), resolution.recipients().size(), broadcast.getSkippedCount());

        return BroadcastCreateRes.builder()
                .broadcastId(broadcast.getId())
                .totalCount(broadcast.getTotalCount())
                .skippedCount(broadcast.getSkippedCount())
                .status(broadcast.getStatus().name())
                .build();
    }

    /**
     * 수신 대상 미리보기 (발송 없이 카운트만)
     *
     * @param dojangId 도장 ID
     * @param req 수신자 조건
     * @return 예상 수신자 수 및 제외 정보
     */
    @Transactional(readOnly = true)
    public RecipientPreviewRes previewRecipients(String dojangId, RecipientPreviewReq req) {
        String tenantId = TenantContextHolder.getTenantId();

        RecipientResolution resolution = resolveRecipients(tenantId, dojangId, req.recipientType(),
                req.sectionIds(), req.divisionIds(), req.studentIds());

        return RecipientPreviewRes.of(
                resolution.totalStudents(),
                resolution.recipients().size(),
                resolution.skippedNoGuardian(),
                resolution.skippedNoPhone());
    }

    /**
     * 실패(DEAD) 메시지 재발송
     *
     * @param dojangId 도장 ID
     * @param broadcastId 발송 ID
     * @return 재발송 대상 건수
     * @throws BusinessException NOT_FOUND - 발송을 찾을 수 없거나 도장이 다른 경우
     * @throws BusinessException NO_FAILED_MESSAGES - 재발송 대상이 없는 경우
     */
    @Transactional
    public ResendFailedRes resendFailed(String dojangId, String broadcastId) {
        MessageBroadcast broadcast = broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new BusinessException(MessageBroadcastErrorCode.NOT_FOUND));

        if (!broadcast.getDojangId().equals(dojangId)) {
            throw new BusinessException(MessageBroadcastErrorCode.NOT_FOUND);
        }

        List<MessageDispatch> deadMessages =
                dispatchRepository.findAllByBroadcastIdAndStatus(broadcastId, MessageStatus.DEAD);

        if (deadMessages.isEmpty()) {
            throw new BusinessException(MessageBroadcastErrorCode.NO_FAILED_MESSAGES);
        }

        deadMessages.forEach(MessageDispatch::resetForResend);

        log.info("실패 메시지 재발송: broadcastId={}, 대상={}건", broadcastId, deadMessages.size());

        return new ResendFailedRes(deadMessages.size());
    }

    private RecipientResolution resolveRecipients(
            String tenantId, String dojangId, RecipientType recipientType,
            List<String> sectionIds, List<String> divisionIds, List<String> studentIds) {

        List<String> candidateStudentIds = findStudentIds(tenantId, dojangId, recipientType,
                sectionIds, divisionIds, studentIds);

        List<String> activeStudentIds = filterActiveStudentIds(tenantId, candidateStudentIds);

        if (activeStudentIds.isEmpty()) {
            return new RecipientResolution(List.of(), 0, 0, 0);
        }

        List<BroadcastRecipientView> guardians = guardianshipRepository.findGuardiansByStudentIds(activeStudentIds);

        long studentsWithGuardian = guardians.stream()
                .map(BroadcastRecipientView::getStudentId)
                .distinct()
                .count();
        int skippedNoGuardian = activeStudentIds.size() - (int) studentsWithGuardian;

        Map<String, ResolvedRecipient> uniqueRecipients = new LinkedHashMap<>();
        int skippedNoPhone = 0;

        for (BroadcastRecipientView g : guardians) {
            if (g.getGuardianPhone() == null || g.getGuardianPhone().isBlank()) {
                if (!uniqueRecipients.containsKey(g.getGuardianId())) {
                    skippedNoPhone++;
                }
                continue;
            }
            uniqueRecipients.putIfAbsent(g.getGuardianId(),
                    new ResolvedRecipient(g.getGuardianId(), g.getStudentId(), g.getGuardianName()));
        }

        return new RecipientResolution(
                new ArrayList<>(uniqueRecipients.values()),
                activeStudentIds.size(),
                skippedNoGuardian,
                skippedNoPhone);
    }

    private List<String> findStudentIds(
            String tenantId, String dojangId, RecipientType recipientType,
            List<String> sectionIds, List<String> divisionIds, List<String> studentIds) {

        return switch (recipientType) {
            case ALL -> studentRepository.findActiveStudentIdsByDojang(tenantId, dojangId, StudentStatus.WITHDRAWN);
            case SECTION -> enrollmentRepository.findStudentIdsBySectionIds(dojangId, sectionIds);
            case DIVISION -> enrollmentRepository.findStudentIdsByDivisionIds(dojangId, divisionIds);
            case INDIVIDUAL -> studentIds != null ? studentIds : List.of();
        };
    }

    private List<String> filterActiveStudentIds(String tenantId, List<String> studentIds) {
        if (studentIds.isEmpty()) {
            return List.of();
        }
        return studentRepository.findActiveStudentIds(studentIds, tenantId, StudentStatus.WITHDRAWN);
    }

    private String serializeCriteria(BroadcastCreateReq req) {
        try {
            return switch (req.recipientType()) {
                case ALL -> "{}";
                case SECTION -> objectMapper.writeValueAsString(Map.of("sectionIds", req.sectionIds()));
                case DIVISION -> objectMapper.writeValueAsString(Map.of("divisionIds", req.divisionIds()));
                case INDIVIDUAL -> objectMapper.writeValueAsString(Map.of("studentIds", req.studentIds()));
            };
        } catch (JsonProcessingException e) {
            throw new BusinessException(MessageBroadcastErrorCode.INVALID_RECIPIENT_TYPE);
        }
    }

    record ResolvedRecipient(String guardianId, String studentId, String guardianName) {}
    record RecipientResolution(List<ResolvedRecipient> recipients, int totalStudents,
                               int skippedNoGuardian, int skippedNoPhone) {}
}
