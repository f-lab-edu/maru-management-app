package com.maru.service.search.analyzer.rule.normalize;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class LowercaseNormalizeRuleTest {

    private LowercaseNormalizeRule rule;

    @BeforeEach
    void setUp() {
        rule = new LowercaseNormalizeRule();
    }

    @ParameterizedTest
    @DisplayName("영문 대문자를 소문자로 변환")
    @CsvSource({
            "MTA, mta",
            "VTA, vta",
            "PREMIUM, premium",
            "Hello, hello",
            "ABC123, abc123"
    })
    void convertToLowercase(String input, String expected) {
        AnalyzeContext context = AnalyzeContext.of(input);

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo(expected);
    }

    @Test
    @DisplayName("한글은 변환 없음")
    void koreanUnchanged() {
        AnalyzeContext context = AnalyzeContext.of("태권도");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("태권도");
    }

    @Test
    @DisplayName("혼합 문자열 - 영문만 소문자로")
    void mixedString() {
        AnalyzeContext context = AnalyzeContext.of("MTA태권도VTA");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("mta태권도vta");
    }

    @Test
    @DisplayName("이미 소문자면 변경 없음")
    void alreadyLowercase() {
        AnalyzeContext context = AnalyzeContext.of("mta");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("mta");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyString() {
        AnalyzeContext context = AnalyzeContext.of("");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEmpty();
    }
}
