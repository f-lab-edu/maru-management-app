package com.maru.domain;

import com.maru.common.exception.BusinessException;
import com.maru.domain.invoice.PaymentLink;
import com.maru.domain.invoice.exception.PgPaymentErrorCode;
import com.maru.fixture.FixtureReflectionUtils;
import com.maru.fixture.PaymentLinkFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentLinkTest {

    @Test
    @DisplayName("결제 링크 생성 시 미사용 + 미만료")
    void createPaymentLink() {
        // given & when
        PaymentLink link = PaymentLinkFixture.aPaymentLink().build();

        // then
        assertThat(link.isUsed()).isFalse();
        assertThat(link.isExpired()).isFalse();
        assertThat(link.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("사용 처리 시 usedAt 세팅")
    void markUsed() {
        // given
        PaymentLink link = PaymentLinkFixture.aPaymentLink().build();

        // when
        link.markUsed();

        // then
        assertThat(link.isUsed()).isTrue();
        assertThat(link.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("만료된 링크 사용 시 예외")
    void expiredLinkValidation() {
        // given
        PaymentLink link = PaymentLinkFixture.aPaymentLink().build();
        FixtureReflectionUtils.setField(link, "expiresAt", LocalDateTime.now().minusHours(1));

        // when & then
        assertThatThrownBy(link::validateUsable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PgPaymentErrorCode.PAYMENT_LINK_EXPIRED);
    }

    @Test
    @DisplayName("이미 사용된 링크 사용 시 예외")
    void usedLinkValidation() {
        // given
        PaymentLink link = PaymentLinkFixture.aPaymentLink().build();
        link.markUsed();

        // when & then
        assertThatThrownBy(link::validateUsable)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PgPaymentErrorCode.PAYMENT_LINK_ALREADY_USED);
    }
}
