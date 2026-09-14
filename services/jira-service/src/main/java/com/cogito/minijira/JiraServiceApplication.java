package com.cogito.minijira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cogito.jiraminijira", "com.cogito.minijira"})
public class JiraServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(JiraServiceApplication.class, args);
    }
}
