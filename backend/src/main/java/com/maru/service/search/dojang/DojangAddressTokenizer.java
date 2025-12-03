package com.maru.service.search.dojang;

import com.maru.service.search.Tokenizer;
import com.maru.service.search.TokenizerEngine;
import com.maru.service.search.rule.normalize.LowercaseNormalizeRule;
import com.maru.service.search.rule.normalize.ParenthesisExtractRule;
import com.maru.service.search.rule.normalize.WhitespaceNormalizeRule;
import com.maru.service.search.rule.segment.AddressDongSegmentRule;
import com.maru.service.search.rule.segment.FallbackSegmentRule;
import com.maru.service.search.rule.tokenize.ChosungTokenizeRule;
import com.maru.service.search.rule.tokenize.FullWordTokenizeRule;
import org.springframework.stereotype.Component;

import java.util.Set;

// 도장 주소 인덱싱용 토크나이저
@Component
public class DojangAddressTokenizer implements Tokenizer {

    private final TokenizerEngine engine;

    public DojangAddressTokenizer() {
        this.engine = TokenizerEngine.builder()
                // 공백 정리, 괄호 내용 추출 (논현동), 소문자 변환
                .normalize(
                        new WhitespaceNormalizeRule(),
                        new ParenthesisExtractRule(),
                        new LowercaseNormalizeRule()
                )
                // 동/읍/면/리 패턴 추출
                .segment(
                        new AddressDongSegmentRule(),
                        new FallbackSegmentRule()
                )
                // 전체 단어, 초성 토큰
                .tokenize(
                        new FullWordTokenizeRule(),
                        new ChosungTokenizeRule()
                )
                .build();
    }

    @Override
    public Set<String> tokenize(String text) {
        return engine.tokenize(text);
    }
}
