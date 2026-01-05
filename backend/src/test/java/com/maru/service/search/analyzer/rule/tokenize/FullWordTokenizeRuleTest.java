package com.maru.service.search.analyzer.rule.tokenize;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FullWordTokenizeRuleTest {

    private FullWordTokenizeRule rule;

    @BeforeEach
    void setUp() {
        rule = new FullWordTokenizeRule();
    }

    @Test
    @DisplayName("세그먼트 전체를 토큰으로 추가")
    void addFullWord() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("태권도");
        context.addSegment("논현");

        rule.apply(context);

        assertThat(context.getTokens()).contains("태권도", "논현");
    }

    @Test
    @DisplayName("빈 세그먼트는 무시")
    void ignoreEmptySegment() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("");
        context.addSegment("태권도");

        rule.apply(context);

        assertThat(context.getTokens()).containsExactly("태권도");
    }

    @Test
    @DisplayName("공백만 있는 세그먼트는 무시")
    void ignoreWhitespaceSegment() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("   ");
        context.addSegment("태권도");

        rule.apply(context);

        assertThat(context.getTokens()).containsExactly("태권도");
    }

    @Test
    @DisplayName("세그먼트 없으면 토큰 없음")
    void noSegmentsNoTokens() {
        AnalyzeContext context = AnalyzeContext.of("test");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }
}
