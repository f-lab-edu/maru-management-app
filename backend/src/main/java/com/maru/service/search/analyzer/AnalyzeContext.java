package com.maru.service.search.analyzer;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class AnalyzeContext {

    @Setter
    private String remainingText;
    private final Set<String> tokens;
    private final Set<AnalyzedToken> analyzedTokens;
    private final List<String> segments;

    private AnalyzeContext(String text) {
        this.remainingText = text;
        this.tokens = new HashSet<>();
        this.analyzedTokens = new HashSet<>();
        this.segments = new ArrayList<>();
    }

    /**
     * @param text 원본 텍스트
     * @return 새로운 AnalyzeContext
     */
    public static AnalyzeContext of(String text) {
        return new AnalyzeContext(text != null ? text.trim() : "");
    }

    public void addAnalyzedToken(String token, MatchType matchType) {
        if (token != null && !token.isBlank()) {
            analyzedTokens.add(new AnalyzedToken(token, matchType));
            tokens.add(token);
        }
    }

    public void addSegment(String segment) {
        if (segment != null && !segment.isBlank()) {
            segments.add(segment);
        }
    }

    public boolean hasSegments() {
        return !segments.isEmpty();
    }
}
