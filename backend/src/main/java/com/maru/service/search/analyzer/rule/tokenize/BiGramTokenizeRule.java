package com.maru.service.search.analyzer.rule.tokenize;

import com.maru.service.search.analyzer.AnalyzeContext;
import com.maru.service.search.analyzer.AnalyzeRule;

/**
 * 한글 Bi-gram 토큰을 생성하는 룰
 * 태권도 → [태권, 권도]
 */
public class BiGramTokenizeRule implements AnalyzeRule {

    private static final int ORDER = 200;
    private static final int MIN_LENGTH = 2;

    @Override
    public void apply(AnalyzeContext context) {
        for (String segment : context.getSegments()) {
            String korean = extractKorean(segment);
            if (korean.length() >= MIN_LENGTH) {
                generateBiGrams(korean, context);
            }
        }
    }

    private String extractKorean(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (isKorean(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean isKorean(char c) {
        return c >= 0xAC00 && c <= 0xD7A3;
    }

    private void generateBiGrams(String text, AnalyzeContext context) {
        for (int i = 0; i <= text.length() - MIN_LENGTH; i++) {
            context.addToken(text.substring(i, i + MIN_LENGTH));
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
