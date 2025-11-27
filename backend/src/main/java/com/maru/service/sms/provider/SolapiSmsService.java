package com.maru.service.sms.provider;

import com.maru.common.exception.BusinessException;
import com.maru.config.properties.SmsProperties;
import com.maru.service.sms.SmsService;

import static com.maru.common.exception.ErrorCode.*;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolapiSmsService implements SmsService {

    private static final String PROVIDER = "SOLAPI";

    private final SmsProperties smsProperties;
    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = SolapiClient.INSTANCE.createInstance(
                smsProperties.apiKey(),
                smsProperties.apiSecret()
        );
        log.info("Solapi SMS 서비스 초기화 완료");
    }

    @Override
    public void send(String phone, String message) {
        Message smsMessage = new Message();
        smsMessage.setFrom(normalizePhoneNumber(smsProperties.senderNumber()));
        smsMessage.setTo(normalizePhoneNumber(phone));
        smsMessage.setText(message);

        try {
            messageService.send(smsMessage);
            log.info("SMS 발송 완료: to={}", phone);
        } catch (Exception e) {
            log.error("SMS 발송 실패: to={}, error={}", phone, e.getMessage());
            throw new BusinessException(SMS_SEND_FAILED);
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
