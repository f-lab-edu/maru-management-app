package com.maru.service.search.rule.tokenize;

import com.maru.service.search.TokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumberTokenizeRuleTest {

    private NumberTokenizeRule rule;

    @BeforeEach
    void setUp() {
        rule = new NumberTokenizeRule();
    }

    @Test
    @DisplayName("숫자+한글 조합 토큰화")
    void numberWithKorean() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("2관");

        rule.apply(context);

        assertThat(context.getTokens()).contains("2관");
    }

    @Test
    @DisplayName("숫자만 있는 경우")
    void numberOnly() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("123");

        rule.apply(context);

        assertThat(context.getTokens()).contains("123");
    }

    @Test
    @DisplayName("한글만 있으면 무시")
    void koreanOnly() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("태권도");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }

    @Test
    @DisplayName("영문만 있으면 무시")
    void englishOnly() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("MTA");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }

    @Test
    @DisplayName("한글+숫자 조합")
    void koreanWithNumber() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("강남1");

        rule.apply(context);

        assertThat(context.getTokens()).contains("강남1");
    }
}
