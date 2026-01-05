package com.maru.service.notification.sender;

import com.maru.domain.message.MessageQueue;
import com.maru.service.notification.MessageSender;
import com.maru.service.sms.SmsService;
import com.maru.service.sms.SmsService.SmsRecipient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SmsMessageSender implements MessageSender {

    private final SmsService smsService;

    @Override
    public void sendBatch(List<MessageQueue> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        List<SmsRecipient> recipients = messages.stream()
                .map(msg -> new SmsRecipient(msg.getRecipientPhone(), msg.getBody()))
                .toList();

        smsService.sendBatch(recipients);
    }
}
