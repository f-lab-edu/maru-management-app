package com.maru.db;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("prod")
class SchemaValidationTest {

    @Test
    void contextLoads_withSchemaValidation() {
        // 빈 테스트: 스프링 컨텍스트 기동 시 JPA ddl-auto=validate가 실행되어
        // 스키마가 불일치하면 예외로 테스트가 실패합니다.
    }
}

