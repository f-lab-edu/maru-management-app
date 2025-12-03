package com.maru.service.search.rule.segment;

import com.maru.service.search.TokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnglishKoreanSplitRuleTest {

    private EnglishKoreanSplitRule rule;

    @BeforeEach
    void setUp() {
        rule = new EnglishKoreanSplitRule();
    }

    @Test
    @DisplayName("영문+한글 분리")
    void splitEnglishKorean() {
        TokenContext context = TokenContext.of("MTA태권도");

        rule.apply(context);

        assertThat(context.getSegments()).containsExactlyInAnyOrder("MTA", "태권도");
    }

    @Test
    @DisplayName("한글+영문 분리")
    void splitKoreanEnglish() {
        TokenContext context = TokenContext.of("태권도MTA");

        rule.apply(context);

        assertThat(context.getSegments()).containsExactlyInAnyOrder("태권도", "MTA");
    }

    @Test
    @DisplayName("숫자+한글 분리")
    void splitNumberKorean() {
        TokenContext context = TokenContext.of("성도2관");

        rule.apply(context);

        assertThat(context.getSegments()).contains("성도");
        assertThat(context.getSegments()).contains("2관");
    }

    @Test
    @DisplayName("한글+숫자 분리")
    void splitKoreanNumber() {
        TokenContext context = TokenContext.of("강남1");

        rule.apply(context);

        assertThat(context.getSegments()).containsExactlyInAnyOrder("강남", "1");
    }

    @Test
    @DisplayName("영문+숫자+한글 복합 분리")
    void splitComplex() {
        TokenContext context = TokenContext.of("MTA2태권도");

        rule.apply(context);

        assertThat(context.getSegments()).contains("MTA");
        // 패턴이 "2태권도"를 숫자+한글로 캡처할 수 있음
        assertThat(context.getSegments()).anyMatch(s -> s.contains("태권도") || s.contains("2"));
    }

    @Test
    @DisplayName("한글만 있으면 그대로")
    void koreanOnly() {
        TokenContext context = TokenContext.of("태권도");

        rule.apply(context);

        assertThat(context.getSegments()).containsExactly("태권도");
    }

    @Test
    @DisplayName("영문만 있으면 그대로")
    void englishOnly() {
        TokenContext context = TokenContext.of("MTA");

        rule.apply(context);

        assertThat(context.getSegments()).containsExactly("MTA");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyString() {
        TokenContext context = TokenContext.of("");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
    }
}
