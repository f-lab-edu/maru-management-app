package com.maru.service.search.dojang.analyzer;

import com.maru.service.search.analyzer.Analyzer;
import com.maru.service.search.analyzer.AnalyzerEngine;
import com.maru.service.search.analyzer.rule.normalize.LowercaseNormalizeRule;
import com.maru.service.search.analyzer.rule.normalize.SpecialCharNormalizeRule;
import com.maru.service.search.analyzer.rule.normalize.WhitespaceNormalizeRule;
import com.maru.service.search.analyzer.rule.segment.EnglishKoreanSplitRule;
import com.maru.service.search.analyzer.rule.segment.FallbackSegmentRule;
import com.maru.service.search.analyzer.rule.segment.PrefixSegmentRule;
import com.maru.service.search.analyzer.rule.segment.SuffixSegmentRule;
import com.maru.service.search.analyzer.rule.tokenize.BiGramTokenizeRule;
import com.maru.service.search.analyzer.rule.tokenize.ChosungTokenizeRule;
import com.maru.service.search.analyzer.rule.tokenize.EnglishTokenizeRule;
import com.maru.service.search.analyzer.rule.tokenize.FullWordTokenizeRule;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DojangNameAnalyzer implements Analyzer {

    private final AnalyzerEngine engine;

    public DojangNameAnalyzer() {
        this.engine = AnalyzerEngine.builder()
                .normalize(
                        new SpecialCharNormalizeRule(),
                        new WhitespaceNormalizeRule(),
                        new LowercaseNormalizeRule()
                )
                .segment(
                        new PrefixSegmentRule(),
                        new SuffixSegmentRule(),
                        new EnglishKoreanSplitRule(),
                        new FallbackSegmentRule()
                )
                .tokenize(
                        new BiGramTokenizeRule(),
                        new FullWordTokenizeRule(),
                        new ChosungTokenizeRule(),
                        new EnglishTokenizeRule()
                )
                .build();
    }

    @Override
    public Set<String> analyze(String text) {
        return engine.analyze(text);
    }
}
