package com.lelebees.imperabot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImperaBotApplication {
    public static final Dotenv dotenv = Dotenv.load();
    public static void main(String[] args) {
        SpringApplication.run(ImperaBotApplication.class, args);
    }

}
