package com.maru.service.search.dojang;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DojangAddressTokenizerTest {

    private DojangAddressTokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new DojangAddressTokenizer();
    }

    @Test
    @DisplayName("기본 주소 토큰화")
    void basicAddress() {
        Set<String> tokens = tokenizer.tokenize("서울 강남구 논현동");

        assertThat(tokens).contains("서울", "강남구", "논현동");
    }

    @Test
    @DisplayName("괄호 안 동 정보 추출")
    void extractDongInParenthesis() {
        Set<String> tokens = tokenizer.tokenize("서울 강남구 강남대로122길 39-9 202호(논현동)");

        assertThat(tokens).contains("논현동");
        assertThat(tokens).contains("ㄴㅎㄷ"); // 논현동 초성
    }

    @Test
    @DisplayName("구 단위 토큰화")
    void guTokenization() {
        Set<String> tokens = tokenizer.tokenize("서울 강남구");

        assertThat(tokens).contains("강남구");
        assertThat(tokens).contains("ㄱㄴㄱ"); // 강남구 초성
    }

    @Test
    @DisplayName("동/읍/면/리 패턴 추출")
    void dongPattern() {
        Set<String> tokens = tokenizer.tokenize("경기 화성시 향남읍 행정동로");

        assertThat(tokens).contains("향남읍");
    }

    @Test
    @DisplayName("복잡한 주소 처리")
    void complexAddress() {
        Set<String> tokens = tokenizer.tokenize("경기 광명시 하안동 광명e-편한세상동부센츠레빌 단지내 상가");

        assertThat(tokens).contains("광명시", "하안동");
    }

    @Test
    @DisplayName("초성 생성")
    void chosungGeneration() {
        Set<String> tokens = tokenizer.tokenize("대치동");

        assertThat(tokens).contains("대치동");
        assertThat(tokens).contains("ㄷㅊㄷ");
    }

    @Test
    @DisplayName("null 입력 처리")
    void nullInput() {
        Set<String> tokens = tokenizer.tokenize(null);

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyInput() {
        Set<String> tokens = tokenizer.tokenize("");

        assertThat(tokens).isEmpty();
    }
}
