package com.maru.service.search.dojang;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DojangNameTokenizerTest {

    private DojangNameTokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new DojangNameTokenizer();
    }

    @Test
    @DisplayName("기본 도장명 토큰화 - 천명태권도장")
    void basicDojangName() {
        Set<String> tokens = tokenizer.tokenize("천명태권도장");

        assertThat(tokens).contains("천명", "태권도장");
        assertThat(tokens).contains("ㅊㅁ"); // 천명 초성
    }

    @Test
    @DisplayName("대학 접두사 분리 - 용인대천명태권도장")
    void universityPrefix() {
        Set<String> tokens = tokenizer.tokenize("용인대천명태권도장");

        assertThat(tokens).contains("용인대", "천명", "태권도장");
    }

    @Test
    @DisplayName("브랜드 접두사 분리 - MTA태권도장")
    void brandPrefix() {
        Set<String> tokens = tokenizer.tokenize("MTA태권도장");

        assertThat(tokens).contains("mta", "태권도장");
    }

    @Test
    @DisplayName("대소문자 무관 검색 지원 - mta, MTA 둘 다")
    void caseInsensitive() {
        Set<String> upperTokens = tokenizer.tokenize("MTA태권도");
        Set<String> lowerTokens = tokenizer.tokenize("mta태권도");

        assertThat(upperTokens).contains("mta");
        assertThat(lowerTokens).contains("mta");
    }

    @Test
    @DisplayName("특수문자 처리 - K-TI태권도")
    void specialCharacter() {
        Set<String> tokens = tokenizer.tokenize("K-TI태권도");

        assertThat(tokens).anyMatch(t -> t.contains("k") || t.contains("ti"));
    }

    @Test
    @DisplayName("바이그램 생성 - 태권도")
    void bigramGeneration() {
        Set<String> tokens = tokenizer.tokenize("태권도");

        assertThat(tokens).contains("태권", "권도");
    }

    @Test
    @DisplayName("초성 생성")
    void chosungGeneration() {
        Set<String> tokens = tokenizer.tokenize("논현체육관");

        assertThat(tokens).contains("ㄴㅎ"); // 논현 초성
        assertThat(tokens).contains("ㅊㅇㄱ"); // 체육관 초성
    }

    @Test
    @DisplayName("숫자 포함 도장명 - 성도2체육관")
    void withNumber() {
        Set<String> tokens = tokenizer.tokenize("성도2체육관");

        assertThat(tokens).contains("성도", "체육관");
        assertThat(tokens).contains("2");
    }

    @ParameterizedTest
    @DisplayName("빈 입력 처리")
    @CsvSource(value = {"''", "' '", "'  '"}, delimiter = ',')
    void emptyInput(String input) {
        Set<String> tokens = tokenizer.tokenize(input);

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("null 입력 처리")
    void nullInput() {
        Set<String> tokens = tokenizer.tokenize(null);

        assertThat(tokens).isEmpty();
    }
}
