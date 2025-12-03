package com.maru.service.search.rule.tokenize;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 영문 토큰을 추출하는 룰
 * 2글자 이상 영문을 소문자로 토큰화
 */
public class EnglishTokenizeRule implements TokenRule {

    private static final int ORDER = 230;
    private static final int MIN_LENGTH = 2;
    // 영문 2글자 이상 연속 매칭
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]{" + MIN_LENGTH + ",}");

    @Override
    public void apply(TokenContext context) {
        for (String segment : context.getSegments()) {
            Matcher matcher = ENGLISH_PATTERN.matcher(segment);
            while (matcher.find()) {
                context.addToken(matcher.group().toLowerCase());
            }
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
