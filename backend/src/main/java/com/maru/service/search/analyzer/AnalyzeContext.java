package com.maru.service.search.analyzer;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class AnalyzeContext {

    private final String originalText;
    @Setter
    private String remainingText;
    private final Set<String> tokens;
    private final List<String> segments;
    private final Map<String, Object> metadata;

    private AnalyzeContext(String text) {
        this.originalText = text;
        this.remainingText = text;
        this.tokens = new HashSet<>();
        this.segments = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    /**
     * @param text 원본 텍스트
     * @return 새로운 AnalyzeContext
     */
    public static AnalyzeContext of(String text) {
        return new AnalyzeContext(text != null ? text.trim() : "");
    }

    public void addToken(String token) {
        if (token != null && !token.isBlank()) {
            tokens.add(token);
        }
    }

    public void addTokens(Set<String> newTokens) {
        if (newTokens != null) {
            newTokens.stream()
                    .filter(t -> t != null && !t.isBlank())
                    .forEach(tokens::add);
        }
    }

    public void addSegment(String segment) {
        if (segment != null && !segment.isBlank()) {
            segments.add(segment);
        }
    }

    public void putMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadataAs(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public boolean hasSegments() {
        return !segments.isEmpty();
    }

    public boolean hasTokens() {
        return !tokens.isEmpty();
    }
}
