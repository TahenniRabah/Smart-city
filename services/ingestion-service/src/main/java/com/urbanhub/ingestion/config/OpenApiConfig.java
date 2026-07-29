package com.urbanhub.ingestion.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String API_KEY_SCHEME = "sensorApiKey";

    @Bean
    public OpenAPI ingestionOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("UrbanHub Ingestion API")
                        .version("1.0.0")
                        .description(
                                "API sécurisée de réception des mesures IoT"
                        )
                )
                .components(new Components()
                        .addSecuritySchemes(
                                API_KEY_SCHEME,
                                new SecurityScheme()
                                        .type(
                                                SecurityScheme.Type.APIKEY
                                        )
                                        .in(
                                                SecurityScheme.In.HEADER
                                        )
                                        .name("X-API-Key")
                        )
                );
    }
}
