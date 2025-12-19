package com.maru.service.notification.sender;

import com.maru.domain.message.MessageQueue;
import com.maru.service.notification.MessageSender;
import com.maru.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmsMessageSender implements MessageSender {

    private final SmsService smsService;

    @Override
    public void send(MessageQueue message) {
        smsService.send(message.getRecipientPhone(), message.getBody());
    }
}