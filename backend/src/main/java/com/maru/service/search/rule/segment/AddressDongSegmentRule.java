package com.maru.service.search.rule.segment;

import com.maru.service.search.TokenContext;
import com.maru.service.search.TokenRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 주소에서 동/읍/면/리를 추출하는 룰
 * 논현동, 역삼동 등 행정구역 추출
 */
public class AddressDongSegmentRule implements TokenRule {

    private static final int ORDER = 120;
    // 한글 + 동/읍/면/리로 끝나는 패턴
    private static final Pattern DONG_PATTERN = Pattern.compile("([가-힣]+[동읍면리])");

    @Override
    public void apply(TokenContext context) {
        String text = context.getRemainingText();
        if (text == null || text.isBlank()) {
            return;
        }

        Matcher matcher = DONG_PATTERN.matcher(text);
        while (matcher.find()) {
            String dong = matcher.group(1);
            context.addSegment(dong);
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
