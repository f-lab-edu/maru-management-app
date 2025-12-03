package com.maru.service.search.rule.segment;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 영문과 한글이 붙어있는 경우 분리하는 룰
 * MTA태권도 → ["MTA", "태권도"]
 */
public class EnglishKoreanSplitRule implements TokenRule {

    private static final int ORDER = 130;
    // 영문 연속 또는 한글 연속을 각각 캡처
    private static final Pattern SPLIT_PATTERN = Pattern.compile("([a-zA-Z]+|[가-힣]+)");

    @Override
    public void apply(TokenContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        Matcher matcher = SPLIT_PATTERN.matcher(text);
        while (matcher.find()) {
            String segment = matcher.group(1);
            if (!segment.isBlank()) {
                context.addSegment(segment);
            }
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
