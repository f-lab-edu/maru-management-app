package com.maru.service.search.analyzer.rule.normalize;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialCharNormalizeRuleTest {

    private SpecialCharNormalizeRule rule;

    @BeforeEach
    void setUp() {
        rule = new SpecialCharNormalizeRule();
    }

    @ParameterizedTest
    @DisplayName("특수문자를 공백으로 치환")
    @CsvSource({
            "K-TI, K TI",
            "V.T.A, V T A",
            "태권·도장, 태권 도장",
            "A_B_C, A B C"
    })
    void replaceSpecialCharsWithSpace(String input, String expected) {
        AnalyzeContext context = AnalyzeContext.of(input);

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo(expected);
    }

    @Test
    @DisplayName("특수문자 없으면 변경 없음")
    void noSpecialChars() {
        AnalyzeContext context = AnalyzeContext.of("태권도장");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("태권도장");
    }

    @Test
    @DisplayName("연속 특수문자 처리")
    void consecutiveSpecialChars() {
        AnalyzeContext context = AnalyzeContext.of("A--B");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("A  B");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyString() {
        AnalyzeContext context = AnalyzeContext.of("");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEmpty();
    }

    @Test
    @DisplayName("복합 케이스 - K-TI 태권도")
    void complexCase() {
        AnalyzeContext context = AnalyzeContext.of("K-TI·태권도");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("K TI 태권도");
    }
}
