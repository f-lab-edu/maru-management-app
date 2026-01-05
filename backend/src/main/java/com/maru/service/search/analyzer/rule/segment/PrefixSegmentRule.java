package com.maru.service.search.analyzer.rule.segment;

import com.maru.service.search.analyzer.AnalyzeContext;
import com.maru.service.search.analyzer.AnalyzeRule;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 접두사를 분리하는 룰
 * 용인대, 경희대, MTA, VTA 등 대학/브랜드 접두사 분리
 */
public class PrefixSegmentRule implements AnalyzeRule {

    private static final int ORDER = 100;

    private static final String[] PREFIX_VALUES = {
            "한국체대", "용인대", "경희대", "고려대", "연세대", "한체대", "동아대", "우석대",
            "PREMIUM", "premium", "MTA", "VTA", "KTI", "K-TI"
    };

    // 길이 내림차순 정렬 (긴 것 우선 매칭)
    private static final List<String> PREFIXES = Stream.of(PREFIX_VALUES)
            .sorted(Comparator.comparingInt(String::length).reversed())
            .toList();

    @Override
    public void apply(AnalyzeContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        for (String prefix : PREFIXES) {
            if (startsWithIgnoreCase(text, prefix)) {
                String lowerPrefix = prefix.toLowerCase();
                context.addToken(lowerPrefix);
                context.addSegment(lowerPrefix);
                context.setRemainingText(text.substring(prefix.length()));
                return;
            }
        }
    }

    private boolean startsWithIgnoreCase(String text, String prefix) {
        if (text.length() < prefix.length()) {
            return false;
        }
        return text.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
