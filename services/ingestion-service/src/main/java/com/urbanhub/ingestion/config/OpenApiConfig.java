package com.urbanhub.ingestion.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ingestionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UrbanHub - Ingestion Service API")
                        .version("1.0.0")
                        .description("API du service d’ingestion pour recevoir les mesures brutes IoT et publier MeasurementReceived."));
    }
}

