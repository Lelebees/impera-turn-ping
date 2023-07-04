package com.lelebees.imperabot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.rest.RestClient;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        // TODO: Learn how to do this with System.env maybe?
        return DiscordClientBuilder.create(env.get("DISCORD_TOKEN")).build()
                .gateway()
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.listening("to /commands")))
                .login()
                .block();
    }

    @Bean
    public RestClient discordRestClient(GatewayDiscordClient client) {
        return client.getRestClient();
    }
}
