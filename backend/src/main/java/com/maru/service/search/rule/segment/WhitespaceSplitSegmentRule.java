package com.maru.service.search.rule.segment;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

// 공백 기준으로 텍스트를 분리하여 세그먼트로 추가하는 룰
public class WhitespaceSplitSegmentRule implements TokenRule {

    private static final int ORDER = 100;

    @Override
    public void apply(TokenContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        String[] parts = text.trim().split("\\s+");
        for (String part : parts) {
            if (!part.isBlank()) {
                context.addSegment(part);
            }
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
