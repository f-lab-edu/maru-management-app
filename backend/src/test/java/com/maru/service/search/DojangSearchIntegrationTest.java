package com.maru.service.search;

import com.maru.service.search.dojang.DojangAddressTokenizer;
import com.maru.service.search.dojang.DojangNameTokenizer;
import com.maru.service.search.dojang.DojangQueryTokenizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class DojangSearchIntegrationTest {

    private static final String CSV_PATH = "src/test/resources/dojang_list.csv";

    private static final DojangNameTokenizer nameTokenizer = new DojangNameTokenizer();
    private static final DojangAddressTokenizer addressTokenizer = new DojangAddressTokenizer();
    private static final DojangQueryTokenizer queryTokenizer = new DojangQueryTokenizer();

    private static final Map<String, Set<Integer>> invertedIndex = new ConcurrentHashMap<>();
    private static final Map<Integer, DojangData> dojangMap = new HashMap<>();

    record DojangData(int id, String name, String address) {}

    @BeforeAll
    static void setUp() throws IOException {
        loadAndIndexCsv();
        System.out.println("=== 인덱싱 완료 ===");
        System.out.println("총 도장 수: " + dojangMap.size());
        System.out.println("총 토큰 수: " + invertedIndex.size());
    }

    private static void loadAndIndexCsv() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(CSV_PATH, StandardCharsets.UTF_8))) {

            String line;
            int id = 0;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = parseCsvLine(line);
                if (parts.length < 2) continue;

                String name = parts[0].trim();
                String address = parts[1].trim();

                DojangData dojang = new DojangData(id, name, address);
                dojangMap.put(id, dojang);

                Set<String> tokens = new HashSet<>();
                tokens.addAll(nameTokenizer.tokenize(name));
                tokens.addAll(addressTokenizer.tokenize(address));

                for (String token : tokens) {
                    if (token == null || token.isBlank()) continue;
                    invertedIndex.computeIfAbsent(token, k -> ConcurrentHashMap.newKeySet())
                            .add(id);
                }

                id++;
            }
        }
    }

    private static String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    private List<DojangData> search(String keyword) {
        Set<String> queryTokens = queryTokenizer.tokenize(keyword);
        if (queryTokens.isEmpty()) {
            return List.of();
        }

        Set<Integer> resultIds = null;

        for (String token : queryTokens) {
            Set<Integer> matchedIds = invertedIndex.get(token);

            if (matchedIds == null || matchedIds.isEmpty()) {
                return List.of();
            }

            if (resultIds == null) {
                resultIds = new HashSet<>(matchedIds);
            } else {
                resultIds.retainAll(matchedIds);
            }

            if (resultIds.isEmpty()) {
                return List.of();
            }
        }

        return resultIds.stream()
                .map(dojangMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Test
    @DisplayName("도장명 검색 - 논현 체육관")
    void searchByName_논현체육관() {
        List<DojangData> results = search("논현 체육관");

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(d -> d.name().contains("논현"));

        printResults("논현 체육관", results);
    }

    @Test
    @DisplayName("초성 검색 - ㄴㅎ")
    void searchByChosung_ㄴㅎ() {
        List<DojangData> results = search("ㄴㅎ");

        assertThat(results).isNotEmpty();

        printResults("ㄴㅎ", results);
    }

    @Test
    @DisplayName("주소 검색 - 강남구")
    void searchByAddress_강남구() {
        List<DojangData> results = search("강남구");

        assertThat(results).isNotEmpty();
        // 초성 "ㄱㄴㄱ"도 검색되어 관악구/금천구 등도 매칭될 수 있음
        assertThat(results).anyMatch(d -> d.address().contains("강남구"));

        printResults("강남구", results);
    }

    @Test
    @DisplayName("괄호 안 동 검색 - 논현동")
    void searchByDong_논현동() {
        List<DojangData> results = search("논현동");

        assertThat(results).isNotEmpty();

        printResults("논현동", results);
    }

    @Test
    @DisplayName("동 초성 검색 - ㄴㅎㄷ")
    void searchByDongChosung_ㄴㅎㄷ() {
        List<DojangData> results = search("ㄴㅎㄷ");

        assertThat(results).isNotEmpty();

        printResults("ㄴㅎㄷ", results);
    }

    @ParameterizedTest
    @DisplayName("다양한 검색어 테스트")
    @CsvSource({
            "태권도, true",
            "체육관, true",
            "MTA, true",
            "mta, true",
            "성도2, true",
            "대치동, true"
    })
    void searchVariousKeywords(String keyword, boolean shouldHaveResults) {
        List<DojangData> results = search(keyword);

        if (shouldHaveResults) {
            assertThat(results).isNotEmpty();
        }

        printResults(keyword, results);
    }

    @Test
    @DisplayName("1글자 검색 - 강")
    void searchSingleChar_강() {
        List<DojangData> results = search("강");

        printResults("강", results);
    }

    private void printResults(String keyword, List<DojangData> results) {
        System.out.println("\n=== 검색어: \"" + keyword + "\" ===");
        System.out.println("결과 수: " + results.size());
        results.stream().limit(5).forEach(d ->
                System.out.println("  - " + d.name() + " | " + d.address())
        );
        if (results.size() > 5) {
            System.out.println("  ... 외 " + (results.size() - 5) + "건");
        }
    }
}
