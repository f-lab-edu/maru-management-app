package com.maru.service.search.analyzer.rule.segment;

import com.maru.service.search.analyzer.AnalyzeContext;
import com.maru.service.search.analyzer.AnalyzeRule;

/**
 * segments가 비어있을 때 원본 텍스트를 segment로 추가하는 안전장치 룰
 * 다른 룰에서 분리되지 않은 경우에도 토큰 생성 보장
 */
public class FallbackSegmentRule implements AnalyzeRule {

    private static final int ORDER = 199;

    @Override
    public void apply(AnalyzeContext context) {
        if (context.hasSegments()) {
            return;
        }

        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        // 공백으로 분리하여 각각 세그먼트로 추가
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
