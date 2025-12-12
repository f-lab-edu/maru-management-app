package com.maru.service.search.rule.tokenize;

import com.maru.service.search.TokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnglishTokenizeRuleTest {

    private EnglishTokenizeRule rule;

    @BeforeEach
    void setUp() {
        rule = new EnglishTokenizeRule();
    }

    @Test
    @DisplayName("영문 세그먼트를 소문자 토큰으로")
    void englishToLowercase() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("MTA");

        rule.apply(context);

        assertThat(context.getTokens()).contains("mta");
    }

    @Test
    @DisplayName("2글자 이상 영문만 토큰화")
    void minTwoChars() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("VTA");
        context.addSegment("A");

        rule.apply(context);

        assertThat(context.getTokens()).contains("vta");
        assertThat(context.getTokens()).doesNotContain("a");
    }

    @Test
    @DisplayName("한글은 무시")
    void ignoreKorean() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("태권도");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }

    @Test
    @DisplayName("숫자는 무시")
    void ignoreNumbers() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("123");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }

    @Test
    @DisplayName("이미 소문자인 경우")
    void alreadyLowercase() {
        TokenContext context = TokenContext.of("test");
        context.addSegment("mta");

        rule.apply(context);

        assertThat(context.getTokens()).contains("mta");
    }
}
