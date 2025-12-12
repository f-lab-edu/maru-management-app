package com.maru.service.search;

import java.util.Set;

public interface Tokenizer {

    /**
     * 텍스트를 토큰화하여 검색용 토큰 집합을 반환
     *
     * @param text 토큰화할 텍스트
     * @return 생성된 토큰 집합
     */
    Set<String> tokenize(String text);
}
