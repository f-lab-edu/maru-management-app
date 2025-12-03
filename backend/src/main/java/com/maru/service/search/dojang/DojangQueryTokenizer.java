package com.maru.service.search.dojang;

import com.maru.service.search.Tokenizer;
import com.maru.service.search.TokenizerEngine;
import com.maru.service.search.rule.normalize.LowercaseNormalizeRule;
import com.maru.service.search.rule.normalize.SpecialCharNormalizeRule;
import com.maru.service.search.rule.normalize.WhitespaceNormalizeRule;
import com.maru.service.search.rule.segment.EnglishKoreanSplitRule;
import com.maru.service.search.rule.segment.FallbackSegmentRule;
import com.maru.service.search.rule.tokenize.ChosungTokenizeRule;
import com.maru.service.search.rule.tokenize.EnglishTokenizeRule;
import com.maru.service.search.rule.tokenize.FullWordTokenizeRule;
import org.springframework.stereotype.Component;

import java.util.Set;

// 검색어 토큰화용 토크나이저
@Component
public class DojangQueryTokenizer implements Tokenizer {

    private final TokenizerEngine engine;

    public DojangQueryTokenizer() {
        this.engine = TokenizerEngine.builder()
                // 특수문자 → 공백, 공백 정리, 소문자 변환
                .normalize(
                        new SpecialCharNormalizeRule(),
                        new WhitespaceNormalizeRule(),
                        new LowercaseNormalizeRule()
                )
                // 영한 분리 (성도2 → 성도, 2), Fallback
                .segment(
                        new EnglishKoreanSplitRule(),
                        new FallbackSegmentRule()
                )
                // 전체 단어, 초성, 영문 토큰
                .tokenize(
                        new FullWordTokenizeRule(),
                        new ChosungTokenizeRule(),
                        new EnglishTokenizeRule()
                )
                .build();
    }

    @Override
    public Set<String> tokenize(String text) {
        return engine.tokenize(text);
    }
}
