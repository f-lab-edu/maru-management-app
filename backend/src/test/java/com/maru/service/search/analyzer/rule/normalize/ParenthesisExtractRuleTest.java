package com.maru.service.search.analyzer.rule.normalize;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParenthesisExtractRuleTest {

    private ParenthesisExtractRule rule;

    @BeforeEach
    void setUp() {
        rule = new ParenthesisExtractRule();
    }

    @Test
    @DisplayName("괄호 안 내용 추출")
    void extractParenthesisContent() {
        AnalyzeContext context = AnalyzeContext.of("서울 강남구 강남대로122길(논현동)");

        rule.apply(context);

        assertThat(context.getSegments()).contains("논현동");
    }

    @Test
    @DisplayName("여러 괄호 처리")
    void multipleParenthesis() {
        AnalyzeContext context = AnalyzeContext.of("(서울)(강남구)(논현동)");

        rule.apply(context);

        assertThat(context.getSegments()).contains("서울", "강남구", "논현동");
    }

    @Test
    @DisplayName("괄호 없으면 변경 없음")
    void noParenthesis() {
        AnalyzeContext context = AnalyzeContext.of("서울 강남구");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
    }

    @Test
    @DisplayName("빈 괄호 무시")
    void emptyParenthesis() {
        AnalyzeContext context = AnalyzeContext.of("서울()강남구");

        rule.apply(context);

        assertThat(context.getSegments()).doesNotContain("");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyString() {
        AnalyzeContext context = AnalyzeContext.of("");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
    }
}
