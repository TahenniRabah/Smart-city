package com.urbanhub.alerting.Config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI airQualityOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UrbanHub - Air Quality Service API")
                        .version("1.0.0")
                        .description("API du microservice Air Quality pour analyser les mesures de pollution et publier des alertes Kafka."));
    }
}

