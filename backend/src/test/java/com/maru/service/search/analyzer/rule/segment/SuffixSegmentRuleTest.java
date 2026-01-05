package com.maru.service.search.analyzer.rule.segment;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SuffixSegmentRuleTest {

    private SuffixSegmentRule rule;

    @BeforeEach
    void setUp() {
        rule = new SuffixSegmentRule();
    }

    @ParameterizedTest
    @DisplayName("접미사 분리")
    @CsvSource({
            "천명태권도장, 천명, 태권도장",
            "강남태권도, 강남, 태권도",
            "논현체육관, 논현, 체육관",
            "백호도장, 백호, 도장"
    })
    void splitSuffix(String input, String expectedName, String expectedSuffix) {
        AnalyzeContext context = AnalyzeContext.of(input);

        rule.apply(context);

        assertThat(context.getSegments()).contains(expectedSuffix);
        assertThat(context.getRemainingText()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("긴 접미사 우선 매칭 - 태권도장 vs 태권도")
    void longerSuffixFirst() {
        AnalyzeContext context = AnalyzeContext.of("천명태권도장");

        rule.apply(context);

        assertThat(context.getSegments()).contains("태권도장");
        assertThat(context.getRemainingText()).isEqualTo("천명");
    }

    @Test
    @DisplayName("접미사 없으면 변경 없음")
    void noSuffix() {
        AnalyzeContext context = AnalyzeContext.of("천명");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
        assertThat(context.getRemainingText()).isEqualTo("천명");
    }

    @Test
    @DisplayName("접미사만 있는 경우 - 세그먼트에 추가")
    void suffixOnly() {
        AnalyzeContext context = AnalyzeContext.of("태권도장");

        rule.apply(context);

        assertThat(context.getSegments()).contains("태권도장");
        assertThat(context.getRemainingText()).isEmpty();
    }

    @Test
    @DisplayName("아카데미 접미사")
    void suffixAcademy() {
        AnalyzeContext context = AnalyzeContext.of("강남아카데미");

        rule.apply(context);

        assertThat(context.getSegments()).contains("아카데미");
        assertThat(context.getRemainingText()).isEqualTo("강남");
    }
}
