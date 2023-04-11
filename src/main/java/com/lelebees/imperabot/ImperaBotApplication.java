package com.lelebees.imperabot;

import com.lelebees.imperabot.application.discord.DiscordService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImperaBotApplication {
    public static final Dotenv dotenv = Dotenv.load();
    private static final DiscordService discordService = new DiscordService();

    public static void main(String[] args) {
        SpringApplication.run(ImperaBotApplication.class, args);
        discordService.run();
    }

}
