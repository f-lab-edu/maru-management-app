package com.maru.service.search.analyzer;

import java.util.Set;

public interface Analyzer {

    /**
     * @param text 분석할 텍스트
     * @return 생성된 토큰 집합
     */
    Set<String> analyze(String text);
}
