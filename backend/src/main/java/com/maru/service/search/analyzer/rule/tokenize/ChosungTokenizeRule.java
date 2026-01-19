package com.maru.service.search.analyzer.rule.tokenize;

import com.maru.service.search.analyzer.AnalyzeContext;
import com.maru.service.search.analyzer.AnalyzeRule;

/**
 * 한글 초성을 추출하여 토큰으로 추가하는 룰
 * 태권도 → ㅌㄱㄷ
 */
public class ChosungTokenizeRule implements AnalyzeRule {

    private static final int ORDER = 220;
    private static final int MIN_LENGTH = 2;

    private static final char[] CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    @Override
    public void apply(AnalyzeContext context) {
        for (String segment : context.getSegments()) {
            String chosung = extractChosung(segment);
            if (chosung.length() >= MIN_LENGTH) {
                context.addToken(chosung);
            }
        }
    }

    private String extractChosung(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (isKorean(c)) {
                int index = (c - 0xAC00) / 588;
                sb.append(CHOSUNG[index]);
            } else if (isChosung(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean isKorean(char c) {
        return c >= 0xAC00 && c <= 0xD7A3;
    }

    private boolean isChosung(char c) {
        for (char ch : CHOSUNG) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
