package com.maru.service.search.analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class AnalyzerEngine implements Analyzer {

    private final List<AnalyzeRule> normalizeRules;
    private final List<AnalyzeRule> segmentRules;
    private final List<AnalyzeRule> tokenizeRules;

    private AnalyzerEngine(Builder builder) {
        this.normalizeRules = sortByOrder(builder.normalizeRules);
        this.segmentRules = sortByOrder(builder.segmentRules);
        this.tokenizeRules = sortByOrder(builder.tokenizeRules);
    }

    /**
     * @param text 분석할 텍스트
     * @return 생성된 토큰 집합
     */
    @Override
    public Set<String> analyze(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        AnalyzeContext context = AnalyzeContext.of(text);

        for (AnalyzeRule rule : normalizeRules) {
            rule.apply(context);
        }

        for (AnalyzeRule rule : segmentRules) {
            rule.apply(context);
        }

        for (AnalyzeRule rule : tokenizeRules) {
            rule.apply(context);
        }

        return context.getTokens();
    }

    private List<AnalyzeRule> sortByOrder(List<AnalyzeRule> rules) {
        List<AnalyzeRule> sorted = new ArrayList<>(rules);
        sorted.sort(Comparator.comparingInt(AnalyzeRule::getOrder));
        return List.copyOf(sorted);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<AnalyzeRule> normalizeRules = new ArrayList<>();
        private final List<AnalyzeRule> segmentRules = new ArrayList<>();
        private final List<AnalyzeRule> tokenizeRules = new ArrayList<>();

        public Builder normalize(AnalyzeRule... rules) {
            normalizeRules.addAll(List.of(rules));
            return this;
        }

        public Builder segment(AnalyzeRule... rules) {
            segmentRules.addAll(List.of(rules));
            return this;
        }

        public Builder tokenize(AnalyzeRule... rules) {
            tokenizeRules.addAll(List.of(rules));
            return this;
        }

        public AnalyzerEngine build() {
            return new AnalyzerEngine(this);
        }
    }
}
