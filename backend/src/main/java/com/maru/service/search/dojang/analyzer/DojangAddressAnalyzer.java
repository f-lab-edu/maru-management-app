package com.maru.service.search.dojang.analyzer;

import com.maru.service.search.analyzer.AnalyzedToken;
import com.maru.service.search.analyzer.Analyzer;
import com.maru.service.search.analyzer.AnalyzerEngine;
import com.maru.service.search.analyzer.rule.normalize.LowercaseNormalizeRule;
import com.maru.service.search.analyzer.rule.normalize.ParenthesisExtractRule;
import com.maru.service.search.analyzer.rule.normalize.WhitespaceNormalizeRule;
import com.maru.service.search.analyzer.rule.segment.AddressDongSegmentRule;
import com.maru.service.search.analyzer.rule.segment.FallbackSegmentRule;
import com.maru.service.search.analyzer.rule.segment.WhitespaceSplitSegmentRule;
import com.maru.service.search.analyzer.rule.tokenize.ChosungTokenizeRule;
import com.maru.service.search.analyzer.rule.tokenize.FullWordTokenizeRule;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DojangAddressAnalyzer implements Analyzer {

    private final AnalyzerEngine engine;

    public DojangAddressAnalyzer() {
        this.engine = AnalyzerEngine.builder()
                .normalize(
                        new WhitespaceNormalizeRule(),
                        new ParenthesisExtractRule(),
                        new LowercaseNormalizeRule()
                )
                .segment(
                        new WhitespaceSplitSegmentRule(),
                        new AddressDongSegmentRule(),
                        new FallbackSegmentRule()
                )
                .tokenize(
                        new FullWordTokenizeRule(),
                        new ChosungTokenizeRule()
                )
                .build();
    }

    @Override
    public Set<String> analyze(String text) {
        return engine.analyze(text);
    }

    @Override
    public Set<AnalyzedToken> analyzeDetailed(String text) {
        return engine.analyzeDetailed(text);
    }
}
