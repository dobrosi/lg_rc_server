package com.github.dobrosi.lgrcserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(HibernateLoggerHints.class)
public class Application {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}