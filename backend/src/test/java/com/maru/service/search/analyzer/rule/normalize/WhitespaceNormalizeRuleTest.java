package com.maru.service.search.analyzer.rule.normalize;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhitespaceNormalizeRuleTest {

    private WhitespaceNormalizeRule rule;

    @BeforeEach
    void setUp() {
        rule = new WhitespaceNormalizeRule();
    }

    @Test
    @DisplayName("다중 공백을 단일 공백으로")
    void multipleSpacesToSingle() {
        AnalyzeContext context = AnalyzeContext.of("서울  강남구   논현동");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("서울 강남구 논현동");
    }

    @Test
    @DisplayName("앞뒤 공백 제거")
    void trimSpaces() {
        AnalyzeContext context = AnalyzeContext.of("  태권도  ");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("태권도");
    }

    @Test
    @DisplayName("탭/개행 문자도 공백으로 변환")
    void tabAndNewline() {
        AnalyzeContext context = AnalyzeContext.of("서울\t강남구\n논현동");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("서울 강남구 논현동");
    }

    @Test
    @DisplayName("공백 없으면 변경 없음")
    void noSpaces() {
        AnalyzeContext context = AnalyzeContext.of("태권도장");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEqualTo("태권도장");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyString() {
        AnalyzeContext context = AnalyzeContext.of("");

        rule.apply(context);

        assertThat(context.getRemainingText()).isEmpty();
    }
}
