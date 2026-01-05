package com.maru.service.search.analyzer.rule.tokenize;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BiGramTokenizeRuleTest {

    private BiGramTokenizeRule rule;

    @BeforeEach
    void setUp() {
        rule = new BiGramTokenizeRule();
    }

    @Test
    @DisplayName("한글 바이그램 생성")
    void generateBigram() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("태권도");

        rule.apply(context);

        assertThat(context.getTokens()).contains("태권", "권도");
    }

    @Test
    @DisplayName("4글자 바이그램")
    void fourCharBigram() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("태권도장");

        rule.apply(context);

        assertThat(context.getTokens()).contains("태권", "권도", "도장");
    }

    @Test
    @DisplayName("2글자는 바이그램 1개")
    void twoCharBigram() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("논현");

        rule.apply(context);

        assertThat(context.getTokens()).contains("논현");
    }

    @Test
    @DisplayName("1글자는 바이그램 생성 안 함")
    void singleCharNoBigram() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("강");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }

    @Test
    @DisplayName("영문은 바이그램 생성 안 함")
    void englishNoBigram() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("MTA");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }

    @Test
    @DisplayName("숫자는 바이그램 생성 안 함")
    void numberNoBigram() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("123");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }
}
