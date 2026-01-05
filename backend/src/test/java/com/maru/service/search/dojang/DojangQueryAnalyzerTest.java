package com.maru.service.search.dojang;

import com.maru.service.search.dojang.analyzer.DojangQueryAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DojangQueryAnalyzerTest {

    private DojangQueryAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new DojangQueryAnalyzer();
    }

    @Test
    @DisplayName("단일 단어 검색어")
    void singleWord() {
        Set<String> tokens = analyzer.analyze("태권도");

        assertThat(tokens).contains("태권도");
        assertThat(tokens).contains("ㅌㄱㄷ");
    }

    @Test
    @DisplayName("복합 검색어 - 공백으로 분리")
    void multipleWords() {
        Set<String> tokens = analyzer.analyze("논현 체육관");

        assertThat(tokens).contains("논현", "체육관");
        assertThat(tokens).contains("ㄴㅎ", "ㅊㅇㄱ");
    }

    @Test
    @DisplayName("대소문자 무관 - MTA, mta")
    void caseInsensitive() {
        Set<String> upperTokens = analyzer.analyze("MTA");
        Set<String> lowerTokens = analyzer.analyze("mta");

        assertThat(upperTokens).contains("mta");
        assertThat(lowerTokens).contains("mta");
    }

    @Test
    @DisplayName("숫자 포함 검색어 - 성도2")
    void withNumber() {
        Set<String> tokens = analyzer.analyze("성도2");

        assertThat(tokens).contains("성도", "2");
        assertThat(tokens).contains("ㅅㄷ");
    }

    @Test
    @DisplayName("초성 검색어 - ㄴㅎ")
    void chosungQuery() {
        Set<String> tokens = analyzer.analyze("ㄴㅎ");

        assertThat(tokens).contains("ㄴㅎ");
    }

    @Test
    @DisplayName("영한 혼합 검색어 - MTA태권도")
    void mixedQuery() {
        Set<String> tokens = analyzer.analyze("MTA태권도");

        assertThat(tokens).contains("mta", "태권도");
    }

    @Test
    @DisplayName("특수문자 처리 - K-TI")
    void specialCharacter() {
        Set<String> tokens = analyzer.analyze("K-TI");

        // 특수문자가 공백으로 치환되어 분리됨
        assertThat(tokens).anyMatch(t -> t.equals("k") || t.equals("ti"));
    }

    @Test
    @DisplayName("null 입력 처리")
    void nullInput() {
        Set<String> tokens = analyzer.analyze(null);

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void emptyInput() {
        Set<String> tokens = analyzer.analyze("");

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("공백만 있는 경우")
    void whitespaceOnly() {
        Set<String> tokens = analyzer.analyze("   ");

        assertThat(tokens).isEmpty();
    }
}
