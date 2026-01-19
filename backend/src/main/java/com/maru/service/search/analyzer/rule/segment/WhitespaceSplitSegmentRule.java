package com.maru.service.search.analyzer.rule.segment;

import com.maru.service.search.analyzer.AnalyzeContext;
import com.maru.service.search.analyzer.AnalyzeRule;

// 공백 기준으로 텍스트를 분리하여 세그먼트로 추가하는 룰
public class WhitespaceSplitSegmentRule implements AnalyzeRule {

    private static final int ORDER = 100;

    @Override
    public void apply(AnalyzeContext context) {
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
