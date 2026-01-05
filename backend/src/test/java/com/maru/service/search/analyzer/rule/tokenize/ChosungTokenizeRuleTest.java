package com.maru.service.search.analyzer.rule.tokenize;

import com.maru.service.search.analyzer.AnalyzeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ChosungTokenizeRuleTest {

    private ChosungTokenizeRule rule;

    @BeforeEach
    void setUp() {
        rule = new ChosungTokenizeRule();
    }

    @ParameterizedTest
    @DisplayName("한글 초성 추출")
    @CsvSource({
            "태권도, ㅌㄱㄷ",
            "논현동, ㄴㅎㄷ",
            "강남구, ㄱㄴㄱ",
            "천명, ㅊㅁ",
            "가나다, ㄱㄴㄷ"
    })
    void extractChosung(String input, String expectedChosung) {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment(input);

        rule.apply(context);

        assertThat(context.getTokens()).contains(expectedChosung);
    }

    @Test
    @DisplayName("영문은 초성 추출 안 함")
    void skipEnglish() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("MTA");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }

    @Test
    @DisplayName("숫자는 초성 추출 안 함")
    void skipNumbers() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("123");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }

    @Test
    @DisplayName("한글+숫자 혼합 시 한글만 초성 추출")
    void mixedKoreanNumber() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("강남1관");

        rule.apply(context);

        assertThat(context.getTokens()).contains("ㄱㄴㄱ");
    }

    @Test
    @DisplayName("이미 초성인 경우 그대로 유지")
    void alreadyChosung() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("ㄱㄴㄷ");

        rule.apply(context);

        assertThat(context.getTokens()).contains("ㄱㄴㄷ");
    }

    @Test
    @DisplayName("1글자는 초성 생성 안 함 (MIN_LENGTH=2)")
    void singleCharNoChosung() {
        AnalyzeContext context = AnalyzeContext.of("test");
        context.addSegment("강");

        rule.apply(context);

        assertThat(context.getTokens()).isEmpty();
    }
}
