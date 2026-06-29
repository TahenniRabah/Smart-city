package com.urbanhub.alerting.Config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI alertingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UrbanHub - Alerting Service API")
                        .version("1.0.0")
                        .description("API du microservice Alerting pour exposer les informations liées aux alertes Smart City."));
    }
}

