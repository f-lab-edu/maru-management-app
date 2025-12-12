package com.maru.service.search.rule.segment;

import com.maru.service.search.TokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhitespaceSplitSegmentRuleTest {

    private WhitespaceSplitSegmentRule rule;

    @BeforeEach
    void setUp() {
        rule = new WhitespaceSplitSegmentRule();
    }

    @Test
    @DisplayName("공백으로 분리")
    void splitByWhitespace() {
        TokenContext context = TokenContext.of("서울 강남구 논현동");

        rule.apply(context);

        assertThat(context.getSegments()).containsExactlyInAnyOrder("서울", "강남구", "논현동");
    }

    @Test
    @DisplayName("다중 공백도 처리")
    void multipleSpaces() {
        TokenContext context = TokenContext.of("서울  강남구   논현동");

        rule.apply(context);

        assertThat(context.getSegments()).containsExactlyInAnyOrder("서울", "강남구", "논현동");
    }

    @Test
    @DisplayName("공백 없으면 전체를 하나의 세그먼트로")
    void noWhitespace() {
        TokenContext context = TokenContext.of("태권도장");

        rule.apply(context);

        assertThat(context.getSegments()).containsExactly("태권도장");
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyString() {
        TokenContext context = TokenContext.of("");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
    }

    @Test
    @DisplayName("공백만 있는 경우")
    void whitespaceOnly() {
        TokenContext context = TokenContext.of("   ");

        rule.apply(context);

        assertThat(context.getSegments()).isEmpty();
    }
}
