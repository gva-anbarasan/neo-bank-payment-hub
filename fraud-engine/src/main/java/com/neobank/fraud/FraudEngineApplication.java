package com.neobank.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FraudEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(FraudEngineApplication.class, args);
    }
}