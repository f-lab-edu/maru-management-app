package com.maru.service.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class TokenizerEngine implements Tokenizer {

    private final List<TokenRule> normalizeRules;
    private final List<TokenRule> segmentRules;
    private final List<TokenRule> tokenizeRules;

    private TokenizerEngine(Builder builder) {
        this.normalizeRules = sortByOrder(builder.normalizeRules);
        this.segmentRules = sortByOrder(builder.segmentRules);
        this.tokenizeRules = sortByOrder(builder.tokenizeRules);
    }

    /**
     * 파이프라인(Normalize → Segment → Tokenize)을 실행하여 토큰 생성
     *
     * @param text 토큰화할 텍스트
     * @return 생성된 토큰 집합, 빈 텍스트인 경우 빈 Set 반환
     */
    @Override
    public Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        TokenContext context = TokenContext.of(text);

        for (TokenRule rule : normalizeRules) {
            rule.apply(context);
        }

        for (TokenRule rule : segmentRules) {
            rule.apply(context);
        }

        for (TokenRule rule : tokenizeRules) {
            rule.apply(context);
        }

        return context.getTokens();
    }

    private List<TokenRule> sortByOrder(List<TokenRule> rules) {
        List<TokenRule> sorted = new ArrayList<>(rules);
        sorted.sort(Comparator.comparingInt(TokenRule::getOrder));
        return List.copyOf(sorted);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<TokenRule> normalizeRules = new ArrayList<>();
        private final List<TokenRule> segmentRules = new ArrayList<>();
        private final List<TokenRule> tokenizeRules = new ArrayList<>();

        public Builder normalize(TokenRule... rules) {
            normalizeRules.addAll(List.of(rules));
            return this;
        }

        public Builder segment(TokenRule... rules) {
            segmentRules.addAll(List.of(rules));
            return this;
        }

        public Builder tokenize(TokenRule... rules) {
            tokenizeRules.addAll(List.of(rules));
            return this;
        }

        public TokenizerEngine build() {
            return new TokenizerEngine(this);
        }
    }
}
