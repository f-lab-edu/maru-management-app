package com.maru.service.search.analyzer.rule.normalize;

import com.maru.service.search.analyzer.AnalyzeContext;
import com.maru.service.search.analyzer.AnalyzeRule;

import java.util.Locale;

/**
 * 영문을 소문자로 변환하는 룰
 */
public class LowercaseNormalizeRule implements AnalyzeRule {

    private static final int ORDER = 40;

    @Override
    public void apply(AnalyzeContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        context.setRemainingText(text.toLowerCase(Locale.ROOT));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
