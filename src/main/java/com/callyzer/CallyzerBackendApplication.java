package com.callyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CallyzerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CallyzerBackendApplication.class, args);
    }
}
