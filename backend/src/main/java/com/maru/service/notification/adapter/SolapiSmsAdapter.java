package com.maru.service.notification.adapter;

import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.MessageDispatch;
import com.maru.repository.guardian.GuardianRepository;
import com.maru.repository.guardian.view.GuardianPhoneView;
import com.maru.service.notification.result.BatchSendResult;
import com.maru.service.notification.result.SendResult;
import com.maru.service.sms.SmsService;
import com.maru.service.sms.SmsService.SmsRecipient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolapiSmsAdapter implements MessageChannelAdapter {

    private final SmsService smsService;
    private final GuardianRepository guardianRepository;

    @Override
    public MessageChannel getChannel() {
        return MessageChannel.SMS;
    }

    @Override
    public BatchSendResult sendBatch(List<MessageDispatch> messages) {
        if (messages == null || messages.isEmpty()) {
            return BatchSendResult.empty();
        }

        Map<String, String> phoneMap = fetchPhoneMap(messages);
        List<SmsRecipient> recipients = buildRecipients(messages, phoneMap);

        if (recipients.isEmpty()) {
            return allFailed(messages, "NO_PHONE", "전화번호 없음");
        }

        try {
            Map<String, String> vendorMessageIds = smsService.sendBatch(recipients);
            return buildSuccessResult(messages, vendorMessageIds);
        } catch (Exception e) {
            log.error("SMS 배치 발송 실패", e);
            return allFailed(messages, "SEND_EXCEPTION", e.getMessage());
        }
    }

    private Map<String, String> fetchPhoneMap(List<MessageDispatch> messages) {
        List<String> guardianIds = messages.stream()
                .map(MessageDispatch::getGuardianId)
                .distinct()
                .toList();

        return guardianRepository.findPhonesByIds(guardianIds).stream()
                .collect(Collectors.toMap(
                        GuardianPhoneView::getId,
                        GuardianPhoneView::getPhone
                ));
    }

    private List<SmsRecipient> buildRecipients(List<MessageDispatch> messages, Map<String, String> phoneMap) {
        return messages.stream()
                .filter(msg -> phoneMap.containsKey(msg.getGuardianId()))
                .filter(msg -> msg.getBody() != null)
                .map(msg -> new SmsRecipient(
                        msg.getId(),
                        phoneMap.get(msg.getGuardianId()),
                        msg.getBody()
                ))
                .toList();
    }

    private BatchSendResult buildSuccessResult(List<MessageDispatch> messages, Map<String, String> vendorMessageIds) {
        Map<String, SendResult> results = new HashMap<>();
        for (MessageDispatch msg : messages) {
            String vendorMessageId = vendorMessageIds.get(msg.getId());
            results.put(msg.getId(), SendResult.success(msg.getId(), vendorMessageId));
        }
        return new BatchSendResult(results);
    }

    private BatchSendResult allFailed(List<MessageDispatch> messages, String errorCode, String errorMessage) {
        Map<String, SendResult> results = new HashMap<>();
        for (MessageDispatch msg : messages) {
            results.put(msg.getId(), SendResult.failure(msg.getId(), errorCode, errorMessage));
        }
        return new BatchSendResult(results);
    }
}
