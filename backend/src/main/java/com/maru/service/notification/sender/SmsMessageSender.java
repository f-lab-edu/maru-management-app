package com.maru.service.notification.sender;

import com.maru.domain.message.MessageQueue;
import com.maru.repository.guardian.GuardianRepository;
import com.maru.repository.guardian.view.GuardianPhoneView;
import com.maru.service.notification.MessageSender;
import com.maru.service.sms.SmsService;
import com.maru.service.sms.SmsService.SmsRecipient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SmsMessageSender implements MessageSender {

    private final SmsService smsService;
    private final GuardianRepository guardianRepository;

    @Override
    public void sendBatch(List<MessageQueue> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        Map<String, String> phoneMap = fetchPhoneMap(messages);

        List<SmsRecipient> recipients = messages.stream()
                .map(msg -> new SmsRecipient(phoneMap.get(msg.getGuardianId()), msg.getBody()))
                .filter(recipient -> recipient.phone() != null)
                .toList();

        if (!recipients.isEmpty()) {
            smsService.sendBatch(recipients);
        }
    }

    private Map<String, String> fetchPhoneMap(List<MessageQueue> messages) {
        List<String> guardianIds = messages.stream()
                .map(MessageQueue::getGuardianId)
                .distinct()
                .toList();

        return guardianRepository.findPhonesByIds(guardianIds).stream()
                .collect(Collectors.toMap(
                        GuardianPhoneView::getId,
                        GuardianPhoneView::getPhone
                ));
    }
}
