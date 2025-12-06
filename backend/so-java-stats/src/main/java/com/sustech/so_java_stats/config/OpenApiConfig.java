package com.sustech.so_java_stats.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StackOverflow Java Stats API")
                        .version("1.0.0")
                        .description("APIs for analyzing Java-tagged Stack Overflow data"));
    }
}
