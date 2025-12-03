package com.maru.service.search.rule.segment;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

/**
 * segments가 비어있을 때 원본 텍스트를 segment로 추가하는 안전장치 룰
 * 다른 룰에서 분리되지 않은 경우에도 토큰 생성 보장
 */
public class FallbackSegmentRule implements TokenRule {

    private static final int ORDER = 199;

    @Override
    public void apply(TokenContext context) {
        if (context.hasSegments()) {
            return;
        }

        String text = context.getRemainingText();
        if (text != null && !text.isBlank()) {
            context.addSegment(text);
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
