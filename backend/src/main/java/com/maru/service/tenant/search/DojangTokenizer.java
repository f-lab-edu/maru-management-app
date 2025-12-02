package com.maru.service.tenant.search;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DojangTokenizer {

    private static final List<String> SUFFIXES = List.of(
            "태권도장", "태권도", "체육관", "아카데미", "스쿨", "도장", "GYM", "gym"
    );

    private static final List<String> PREFIXES = List.of(
            "용인대", "경희대", "고려대", "연세대", "한체대", "한국체대", "동아대", "우석대",
            "MTA", "VTA", "K-TI", "KTI", "PREMIUM", "premium"
    );

    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]{3,}");
    private static final Pattern DONG_PATTERN = Pattern.compile("(\\S+[동읍면리])");

    private static final char[] CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    /**
     * 도장명 토큰화
     */
    public Set<String> tokenizeName(String name) {
        if (name == null || name.isBlank()) {
            return Set.of();
        }

        Set<String> tokens = new HashSet<>();
        String remaining = name.trim();

        // 1. 접미사 분리
        for (String suffix : SUFFIXES) {
            if (remaining.endsWith(suffix)) {
                remaining = remaining.substring(0, remaining.length() - suffix.length());
                break;
            }
        }

        // 2. 접두사 분리
        for (String prefix : PREFIXES) {
            if (remaining.startsWith(prefix)) {
                tokens.add(prefix.toLowerCase());
                tokens.add(extractChosung(prefix));
                remaining = remaining.substring(prefix.length());
                break;
            }
        }

        // 3. 공백으로 분리
        String[] parts = remaining.trim().split("\\s+");
        for (String part : parts) {
            if (part.isBlank()) continue;
            tokenizePart(part, tokens);
        }

        return tokens;
    }

    /**
     * 주소 토큰화
     */
    public Set<String> tokenizeAddress(String address) {
        if (address == null || address.isBlank()) {
            return Set.of();
        }

        Set<String> tokens = new HashSet<>();

        // 1. 공백 단위로 분리
        String[] parts = address.trim().split("\\s+");
        for (String part : parts) {
            if (part.isBlank()) continue;
            tokens.add(part.toLowerCase());
        }

        // 2. 동/읍/면/리 추출 및 초성 추가
        Matcher matcher = DONG_PATTERN.matcher(address);
        while (matcher.find()) {
            String dong = matcher.group(1);
            tokens.add(dong);
            tokens.add(extractChosung(dong));
        }

        return tokens;
    }

    /**
     * 검색어 토큰화 (사용자 입력)
     */
    public Set<String> tokenizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return Set.of();
        }

        Set<String> tokens = new HashSet<>();
        String normalized = query.trim().toLowerCase();

        // 초성만 입력한 경우 그대로 추가
        if (isChosungOnly(normalized)) {
            tokens.add(normalized);
            return tokens;
        }

        // 공백으로 분리 후 각각 토큰화
        String[] parts = normalized.split("\\s+");
        for (String part : parts) {
            if (part.isBlank()) continue;
            tokens.add(part);
            tokens.add(extractChosung(part));
        }

        return tokens;
    }

    private void tokenizePart(String part, Set<String> tokens) {
        // 영문 3글자 이상은 그대로 저장
        Matcher englishMatcher = ENGLISH_PATTERN.matcher(part);
        while (englishMatcher.find()) {
            tokens.add(englishMatcher.group().toLowerCase());
        }

        // 한글 부분 추출
        String korean = part.replaceAll("[a-zA-Z0-9]", "");
        if (korean.isBlank()) return;

        // 2글자 이상이면 Bi-gram + 초성
        if (korean.length() >= 2) {
            for (int i = 0; i <= korean.length() - 2; i++) {
                tokens.add(korean.substring(i, i + 2));
            }
        }

        // 전체 단어와 초성도 추가
        tokens.add(korean);
        tokens.add(extractChosung(korean));
    }

    /**
     * 초성 추출
     */
    public String extractChosung(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int index = (c - 0xAC00) / 588;
                sb.append(CHOSUNG[index]);
            } else if (isChosung(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean isChosungOnly(String text) {
        for (char c : text.toCharArray()) {
            if (!isChosung(c)) {
                return false;
            }
        }
        return !text.isEmpty();
    }

    private boolean isChosung(char c) {
        for (char chosung : CHOSUNG) {
            if (c == chosung) return true;
        }
        return false;
    }
}
