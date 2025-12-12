package com.maru.service.search.rule.tokenize;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

/**
 * 각 segment 전체를 토큰으로 추가하는 룰
 * 1글자 한글도 포함 (강, 수 등)
 */
public class FullWordTokenizeRule implements TokenRule {

    private static final int ORDER = 210;

    @Override
    public void apply(TokenContext context) {
        for (String segment : context.getSegments()) {
            if (!segment.isBlank()) {
                context.addToken(segment.toLowerCase());
            }
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
