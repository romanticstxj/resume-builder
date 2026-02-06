package com.resume.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resumeBuilderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Resume Builder API")
                        .description("简历构建系统 API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Resume Builder Team")
                                .email("support@resumebuilder.com")));
    }
}
