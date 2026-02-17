package com.maru.service.search.dojang;

import com.maru.service.search.analyzer.MatchType;

public record TokenEntry(String dojangId, IndexField field, MatchType matchType) {
}
