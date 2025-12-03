package com.maru.service.search.rule.normalize;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

/**
 * 영문을 소문자로 변환하는 룰
 */
public class LowercaseNormalizeRule implements TokenRule {

    private static final int ORDER = 40;

    @Override
    public void apply(TokenContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        context.setRemainingText(text.toLowerCase());
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
