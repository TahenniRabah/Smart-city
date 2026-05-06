package com.urbanhub.alerting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class AlertingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertingServiceApplication.class, args);
    }

}
