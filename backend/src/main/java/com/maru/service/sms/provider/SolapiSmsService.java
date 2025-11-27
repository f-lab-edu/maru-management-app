package com.maru.service.sms.provider;

import com.maru.service.sms.SmsService;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SolapiSmsService implements SmsService {

    private static final String PROVIDER = "SOLAPI";

    @Value("${app.sms.solapi.api-key}")
    private String apiKey;

    @Value("${app.sms.solapi.api-secret}")
    private String apiSecret;

    @Value("${app.sms.solapi.sender-number}")
    private String senderNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
        log.info("Solapi SMS 서비스 초기화 완료");
    }

    @Override
    public void send(String phone, String message) {
        Message smsMessage = new Message();
        smsMessage.setFrom(normalizePhoneNumber(senderNumber));
        smsMessage.setTo(normalizePhoneNumber(phone));
        smsMessage.setText(message);

        try {
            messageService.send(smsMessage);
            log.info("SMS 발송 완료: to={}", phone);
        } catch (Exception e) {
            log.error("SMS 발송 실패: to={}, error={}", phone, e.getMessage());
            throw new IllegalStateException("SMS 발송 실패", e);
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }

    private String normalizePhoneNumber(String phone) {
        return phone.replaceAll("-", "");
    }
}
