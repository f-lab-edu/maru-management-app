package com.maru.service.search.analyzer.rule.segment;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixSegmentRuleTest {

    private PrefixSegmentRule rule;

    @BeforeEach
    void setUp() {
        rule = new PrefixSegmentRule();
    }

    @ParameterizedTest
    @DisplayName("대학명 접두사 분리")
    @CsvSource({
            "용인대천명, 용인대, 천명",
            "경희대태권, 경희대, 태권",
            "한체대강남, 한체대, 강남",
            "고려대백호, 고려대, 백호"
    })
    void splitUniversityPrefix(String input, String expectedPrefix, String expectedRemaining) {
        AnalyzeContext context = AnalyzeContext.of(input);

        rule.apply(context);

        assertThat(context.getSegments()).contains(expectedPrefix.toLowerCase());
        assertThat(context.getRemainingText()).isEqualTo(expectedRemaining);
    }

    @ParameterizedTest
    @DisplayName("브랜드명 접두사 분리 - 대소문자 무관")
    @CsvSource({
            "MTA태권도, mta, 태권도",
            "mta태권도, mta, 태권도",
            "VTA논현, vta, 논현",
            "PREMIUM강남, premium, 강남"
    })
    void splitBrandPrefix_caseInsensitive(String input, String expectedPrefix, String expectedRemaining) {
        AnalyzeContext context = AnalyzeContext.of(input);

        rule.apply(context);

        assertThat(context.getSegments()).contains(expectedPrefix);
        assertThat(context.getRemainingText()).isEqualTo(expectedRemaining);
    }

    @Test
    @DisplayName("접두사 없으면 변경 없음")
    void noPrefix() {
        AnalyzeContext context = AnalyzeContext.of("천명태권도");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
        assertThat(context.getRemainingText()).isEqualTo("천명태권도");
    }

    @Test
    @DisplayName("긴 접두사 우선 매칭")
    void longerPrefixFirst() {
        AnalyzeContext context = AnalyzeContext.of("한국체대강남");

        rule.apply(context);

        assertThat(context.getSegments()).contains("한국체대");
        assertThat(context.getRemainingText()).isEqualTo("강남");
    }

    @Test
    @DisplayName("접두사만 있는 경우")
    void prefixOnly() {
        AnalyzeContext context = AnalyzeContext.of("용인대");

        rule.apply(context);

        assertThat(context.getSegments()).contains("용인대");
        assertThat(context.getRemainingText()).isEmpty();
    }
}
