package com.maru.service.search.rule.normalize;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

/**
 * 특수문자를 정규화하는 룰
 * V.T.A → V T A, K-TI → K TI 등 특수문자를 공백으로 대체하여 단어 분리 유지
 */
public class SpecialCharNormalizeRule implements TokenRule {

    private static final int ORDER = 10;
    private static final String[] SPECIAL_CHARS = {".", "-", "·", "_"};

    @Override
    public void apply(TokenContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        String normalized = text;
        for (String ch : SPECIAL_CHARS) {
            normalized = normalized.replace(ch, " ");
        }
        context.setRemainingText(normalized);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
