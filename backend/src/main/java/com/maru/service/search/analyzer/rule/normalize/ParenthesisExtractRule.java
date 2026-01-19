package com.maru.service.search.analyzer.rule.normalize;

import com.maru.service.search.analyzer.AnalyzeContext;
import com.maru.service.search.analyzer.AnalyzeRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 괄호 내용을 추출하는 룰
 * (논현동) → segments에 "논현동" 추가, 괄호 제거
 */
public class ParenthesisExtractRule implements AnalyzeRule {

    private static final int ORDER = 30;
    // 괄호와 그 안의 내용을 매칭: (내용) → 그룹1에 "내용" 캡처
    private static final Pattern PARENTHESIS_PATTERN = Pattern.compile("\\(([^)]+)\\)");

    @Override
    public void apply(AnalyzeContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        Matcher matcher = PARENTHESIS_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String content = matcher.group(1).trim();
            if (!content.isBlank()) {
                context.addSegment(content);
            }
            matcher.appendReplacement(sb, " ");
        }
        matcher.appendTail(sb);

        context.setRemainingText(sb.toString().trim());
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
