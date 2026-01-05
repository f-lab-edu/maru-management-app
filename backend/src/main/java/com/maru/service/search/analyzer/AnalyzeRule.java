package com.maru.service.search.analyzer;

public interface AnalyzeRule {

    /**
     * @param context 분석 컨텍스트
     */
    void apply(AnalyzeContext context);

    /**
     * @return 실행 순서 (낮을수록 먼저 실행)
     */
    int getOrder();
}
