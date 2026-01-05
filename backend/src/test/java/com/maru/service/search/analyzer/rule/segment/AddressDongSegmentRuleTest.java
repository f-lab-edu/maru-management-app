package com.maru.service.search.analyzer.rule.segment;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class AddressDongSegmentRuleTest {

    private AddressDongSegmentRule rule;

    @BeforeEach
    void setUp() {
        rule = new AddressDongSegmentRule();
    }

    @ParameterizedTest
    @DisplayName("동/읍/면/리 패턴 추출")
    @CsvSource({
            "논현동123번지, 논현동",
            "향남읍 행정동로, 향남읍",
            "신림면 중앙로, 신림면",
            "행정리 마을회관, 행정리"
    })
    void extractDongPattern(String input, String expectedDong) {
        AnalyzeContext context = AnalyzeContext.of(input);

        rule.apply(context);

        assertThat(context.getSegments()).contains(expectedDong);
    }

    @Test
    @DisplayName("동 패턴 없으면 변경 없음")
    void noDongPattern() {
        AnalyzeContext context = AnalyzeContext.of("서울 강남구");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
    }

    @Test
    @DisplayName("여러 동 패턴 추출")
    void multipleDongPatterns() {
        AnalyzeContext context = AnalyzeContext.of("논현동에서 대치동까지");

        rule.apply(context);

        assertThat(context.getSegments()).contains("논현동", "대치동");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyString() {
        AnalyzeContext context = AnalyzeContext.of("");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
    }
}
