package com.maru.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_JWT = "JWT";
    private static final String SECURITY_BEARER = "bearer";
    private static final String API_DESCRIPTION = "태권도 도장 관리 시스템 API 문서";
    private static final String DOCS_TITLE = "마루 도장관리 API";
    private static final String DOCS_VERSION = "v1";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(DOCS_TITLE)
                        .description(API_DESCRIPTION)
                        .version(DOCS_VERSION))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_JWT))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_JWT,
                                new SecurityScheme()
                                        .name(SECURITY_JWT)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(SECURITY_BEARER)
                                        .bearerFormat(SECURITY_JWT)));
    }
}
