package com.maru.service.search.rule.tokenize;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 숫자 포함 토큰을 추출하는 룰
 * 1관, 2호점, 301호 등 숫자+한글 조합 처리
 */
public class NumberTokenizeRule implements TokenRule {

    private static final int ORDER = 240;
    // 숫자 + 한글 조합 또는 숫자만 매칭
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+[가-힣]*|[가-힣]*\\d+");

    @Override
    public void apply(TokenContext context) {
        for (String segment : context.getSegments()) {
            Matcher matcher = NUMBER_PATTERN.matcher(segment);
            while (matcher.find()) {
                String token = matcher.group();
                if (!token.isBlank()) {
                    context.addToken(token);
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
