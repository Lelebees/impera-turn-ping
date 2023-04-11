package com.lelebees.imperabot.application.discord;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.gateway.GatewayOptions;
import discord4j.gateway.intent.IntentSet;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.lelebees.imperabot.ImperaBotApplication.dotenv;

@Service
public class DiscordService {

    public void run() {
        GatewayBootstrap<GatewayOptions> client = DiscordClient.create(dotenv.get("DISCORD_TOKEN")).gateway().setEnabledIntents(IntentSet.nonPrivileged()).setInitialPresence(s -> ClientPresence.online());
        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
            //TODO:Handle all commands!
            //Command handling!
            Mono<Void> handleSlashCommands = gateway.on(ChatInputInteractionEvent.class, event -> switch (event.getCommandName()) {
                case "test2" -> event.reply("Electric Boogaloo!");
                case "link" -> event.reply("WIP");
                default -> null;
            }).then();

            Mono<Void> printOnLogin =
                    gateway.on(ReadyEvent.class, ready -> Mono.fromRunnable(() -> {
                                final User self = ready.getSelf();
                                System.out.println("[D] Logged in as " + self.getUsername() + "#" + self.getDiscriminator());
                            }
                    )).then();
            return printOnLogin.and(handleSlashCommands);
        });
        login.block();
    }
}
