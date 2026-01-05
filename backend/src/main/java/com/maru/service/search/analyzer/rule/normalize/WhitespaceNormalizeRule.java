package com.maru.service.search.analyzer.rule.normalize;

import com.maru.service.search.analyzer.AnalyzeContext;
import com.maru.service.search.analyzer.AnalyzeRule;

import java.util.regex.Pattern;

/**
 * 공백을 정규화하는 룰
 * 다중 공백 → 단일 공백, 앞뒤 공백 제거, 탭/개행 → 공백
 */
public class WhitespaceNormalizeRule implements AnalyzeRule {

    private static final int ORDER = 20;
    // 연속된 공백문자(스페이스, 탭, 개행 등)를 매칭
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    @Override
    public void apply(AnalyzeContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        String normalized = WHITESPACE_PATTERN.matcher(text).replaceAll(" ").trim();
        context.setRemainingText(normalized);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
