package com.lelebees.imperabot;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImperaBotApplication {
    public static final Dotenv env = Dotenv.load();

    public static void main(String[] args) {
        SpringApplication.run(ImperaBotApplication.class, args);
    }

    @PostConstruct
    public void runOrdinaryApp() {
        System.out.println("System up and running");
    }
}
