package com.urbanhub.quality.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI qualityOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UrbanHub - Quality Service API")
                        .version("1.0.0")
                        .description("API du service qualité pour superviser la validation des mesures IoT."));
    }
}