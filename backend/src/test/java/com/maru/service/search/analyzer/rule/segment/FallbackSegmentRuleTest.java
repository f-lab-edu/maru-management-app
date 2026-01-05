package com.maru.service.search.analyzer.rule.segment;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FallbackSegmentRuleTest {

    private FallbackSegmentRule rule;

    @BeforeEach
    void setUp() {
        rule = new FallbackSegmentRule();
    }

    @Test
    @DisplayName("세그먼트 없으면 공백 분리 후 추가")
    void addSegmentWhenEmpty() {
        AnalyzeContext context = AnalyzeContext.of("논현 체육관");

        rule.apply(context);

        assertThat(context.getSegments()).contains("논현", "체육관");
    }

    @Test
    @DisplayName("이미 세그먼트 있으면 추가 안 함")
    void skipWhenSegmentsExist() {
        AnalyzeContext context = AnalyzeContext.of("테스트");
        context.addSegment("기존세그먼트");

        rule.apply(context);

        assertThat(context.getSegments()).containsExactly("기존세그먼트");
    }

    @Test
    @DisplayName("단일 단어도 세그먼트로 추가")
    void singleWord() {
        AnalyzeContext context = AnalyzeContext.of("태권도");

        rule.apply(context);

        assertThat(context.getSegments()).contains("태권도");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyString() {
        AnalyzeContext context = AnalyzeContext.of("");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
    }

    @Test
    @DisplayName("공백만 있는 경우")
    void whitespaceOnly() {
        AnalyzeContext context = AnalyzeContext.of("   ");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
    }
}
