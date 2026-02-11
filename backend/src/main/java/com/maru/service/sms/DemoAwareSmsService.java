package com.maru.service.sms;

import com.maru.common.util.MaskingUtil;
import com.maru.security.JwtClaims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Primary
@Component
public class DemoAwareSmsService implements SmsService {

    private static final String PROVIDER = "DEMO_AWARE";

    private final SmsService delegate;
    private final String demoUserId;

    public DemoAwareSmsService(
        @Qualifier("solapiSmsService") SmsService delegate,
        @Value("${maru.demo.user-id:}") String demoUserId
    ) {
        this.delegate = delegate;
        this.demoUserId = demoUserId;
    }

    @Override
    public void send(String phone, String message) {
        if (isDemoUser()) {
            log.info("데모 사용자 SMS 발송 스킵: to={}", MaskingUtil.phone(phone));
            return;
        }
        delegate.send(phone, message);
    }

    @Override
    public Map<String, String> sendBatch(List<SmsRecipient> recipients) {
        if (isDemoUser()) {
            log.info("데모 사용자 SMS 배치 발송 스킵: {}건", recipients != null ? recipients.size() : 0);
            return Map.of();
        }
        return delegate.sendBatch(recipients);
    }

    @Override
    public String getProviderName() {
        return isDemoUser() ? PROVIDER : delegate.getProviderName();
    }

    private boolean isDemoUser() {
        if (demoUserId == null || demoUserId.isBlank()) {
            return false;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        if (auth.getPrincipal() instanceof JwtClaims claims) {
            return demoUserId.equals(claims.userId());
        }

        return false;
    }
}
