package com.urbanhub.airquality;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class AirQualityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirQualityServiceApplication.class, args);
    }

}
