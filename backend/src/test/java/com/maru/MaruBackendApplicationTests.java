package com.maru;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=",
    "spring.datasource.driver-class-name=",
    "spring.flyway.enabled=false"
})
class MaruBackendApplicationTests {

    @Test
    void contextLoads() {
        // 기본 컨텍스트 로드 테스트 - DataSource 없이 실행
    }

}
