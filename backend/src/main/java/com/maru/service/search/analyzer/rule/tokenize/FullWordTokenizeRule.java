package com.maru.service.search.analyzer.rule.tokenize;

import com.maru.service.search.analyzer.AnalyzeContext;
import com.maru.service.search.analyzer.AnalyzeRule;
import com.maru.service.search.analyzer.MatchType;

/**
 * 각 segment 전체를 토큰으로 추가하는 룰
 * 1글자 한글도 포함 (강, 수 등)
 */
public class FullWordTokenizeRule implements AnalyzeRule {

    private static final int ORDER = 210;

    @Override
    public void apply(AnalyzeContext context) {
        for (String segment : context.getSegments()) {
            if (!segment.isBlank()) {
                context.addAnalyzedToken(segment.toLowerCase(), MatchType.FULLWORD);
            }
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
