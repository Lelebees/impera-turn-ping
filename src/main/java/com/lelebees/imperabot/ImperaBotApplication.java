package com.lelebees.imperabot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ImperaBotApplication {
    private final Logger logger = LoggerFactory.getLogger(ImperaBotApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ImperaBotApplication.class, args);
    }

    @Bean
    public GatewayDiscordClient gatewayDiscordClient(@Value("${discord.token}") String discordToken) {
        return DiscordClientBuilder.create(discordToken).build()
                .gateway()
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.watching("banners burn")))
                .login()
                .block();
    }

    @Bean
    public RestClient discordRestClient(GatewayDiscordClient client) {
        return client.getRestClient();
    }
}
