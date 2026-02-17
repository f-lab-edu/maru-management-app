package com.maru.service.sms.provider;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.sms.SmsErrorCode;
import com.maru.common.util.MaskingUtil;
import com.maru.config.properties.SmsProperties;
import com.maru.service.sms.SmsService;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("solapiSmsService")
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

    /**
     * Solapi를 통해 SMS 메시지 발송
     *
     * @param phone 수신자 전화번호
     * @param message 발송할 메시지
     * @throws BusinessException SMS_SEND_FAILED - 발송 실패 시
     */
    @Override
    public void send(String phone, String message) {
        Message smsMessage = buildMessage(phone, message);
        sendWithErrorHandling(smsMessage, phone);
    }

    /**
     * Solapi를 통해 SMS 메시지 배치 발송
     *
     * @param recipients 수신자 목록
     * @return dispatchId → vendorMessageId(groupId) 매핑
     * @throws BusinessException SMS_SEND_FAILED - 발송 실패 시
     */
    @Override
    public Map<String, String> sendBatch(List<SmsRecipient> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            return Map.of();
        }

        ArrayList<Message> messages = new ArrayList<>();
        for (SmsRecipient recipient : recipients) {
            messages.add(buildMessage(recipient.phone(), recipient.message()));
        }

        return sendBatchWithErrorHandling(messages, recipients);
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }

    private Message buildMessage(String phone, String message) {
        Message smsMessage = new Message();
        smsMessage.setFrom(normalizePhoneNumber(smsProperties.senderNumber()));
        smsMessage.setTo(normalizePhoneNumber(phone));
        smsMessage.setText(message);
        return smsMessage;
    }

    private void sendWithErrorHandling(Message smsMessage, String phone) {
        try {
            messageService.send(smsMessage);
            log.info("SMS 발송 완료: to={}", MaskingUtil.phone(phone));
        } catch (Exception e) {
            log.warn("SMS 발송 실패: to={}, error={}", MaskingUtil.phone(phone), e.getMessage());
            throw new BusinessException(SmsErrorCode.SEND_FAILED);
        }
    }

    private Map<String, String> sendBatchWithErrorHandling(ArrayList<Message> messages, List<SmsRecipient> recipients) {
        try {
            MultipleDetailMessageSentResponse response = messageService.send(messages);
            String groupId = extractGroupId(response);
            log.info("SMS 배치 발송 완료: {}건, groupId={}", messages.size(), groupId);
            return mapDispatchIdsToGroupId(recipients, groupId);
        } catch (Exception e) {
            log.warn("SMS 배치 발송 실패: {}건, error={}", messages.size(), e.getMessage());
            throw new BusinessException(SmsErrorCode.SEND_FAILED);
        }
    }

    private String extractGroupId(MultipleDetailMessageSentResponse response) {
        if (response == null || response.getGroupInfo() == null) {
            return null;
        }
        return response.getGroupInfo().getGroupId();
    }

    private Map<String, String> mapDispatchIdsToGroupId(List<SmsRecipient> recipients, String groupId) {
        Map<String, String> result = new HashMap<>();
        if (groupId == null) {
            return result;
        }
        for (SmsRecipient recipient : recipients) {
            result.put(recipient.dispatchId(), groupId);
        }
        return result;
    }

    private String normalizePhoneNumber(String phone) {
        return phone.replaceAll("-", "");
    }
}
