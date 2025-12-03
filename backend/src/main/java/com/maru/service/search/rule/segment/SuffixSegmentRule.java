package com.maru.service.search.rule.segment;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 접미사를 분리하는 룰
 * 태권도장, 체육관, 아카데미 등 도장 유형 접미사 분리
 */
public class SuffixSegmentRule implements TokenRule {

    private static final int ORDER = 110;

    private static final String[] SUFFIX_VALUES = {
            "태권도장", "태권도", "체육관", "아카데미", "스쿨", "도장", "GYM", "gym"
    };

    // 길이 내림차순 정렬 (긴 것 우선 매칭)
    private static final List<String> SUFFIXES = Stream.of(SUFFIX_VALUES)
            .sorted(Comparator.comparingInt(String::length).reversed())
            .toList();

    @Override
    public void apply(TokenContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        for (String suffix : SUFFIXES) {
            if (endsWithIgnoreCase(text, suffix)) {
                String lowerSuffix = suffix.toLowerCase();
                context.addToken(lowerSuffix);
                context.addSegment(lowerSuffix);
                context.setRemainingText(text.substring(0, text.length() - suffix.length()));
                return;
            }
        }
    }

    private boolean endsWithIgnoreCase(String text, String suffix) {
        if (text.length() < suffix.length()) {
            return false;
        }
        int offset = text.length() - suffix.length();
        return text.regionMatches(true, offset, suffix, 0, suffix.length());
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
