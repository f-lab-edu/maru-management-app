package com.maru.service.search;

public interface TokenRule {

    /**
     * 컨텍스트에 규칙 적용
     *
     * @param context 토큰화 컨텍스트
     */
    void apply(TokenContext context);

    /**
     * 규칙의 실행 순서를 반환 (낮을수록 먼저 실행)
     *
     * @return 실행 순서
     */
    int getOrder();
}
