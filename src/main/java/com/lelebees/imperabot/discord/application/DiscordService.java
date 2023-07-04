package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
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

import static com.lelebees.imperabot.ImperaBotApplication.env;

@Service
public class DiscordService {
    UserService userService;

    public DiscordService(UserService userService) {
        this.userService = userService;
    }

    public void run() {
        GatewayBootstrap<GatewayOptions> client = DiscordClient.create(env.get("DISCORD_TOKEN")).gateway().setEnabledIntents(IntentSet.nonPrivileged()).setInitialPresence(s -> ClientPresence.online());
        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
            //TODO:Handle all commands!
            //Command handling!
            Mono<Void> handleSlashCommands = gateway.on(ChatInputInteractionEvent.class, event -> switch (event.getCommandName()) {
                case "test2" -> event.reply("Electric Boogaloo!");
                case "link" ->
                        event.reply("WIP" + getLinkCode(event.getInteraction().getMember().get().getMemberData().user().id().asLong()));
                case "unlink" -> event.reply("WIP");
                case "track" -> event.reply("WIP");
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

    public String getLinkCode(long callerId) {
        BotUser botUser;
        try {
            botUser = userService.findUser(callerId);
        } catch (UserNotFoundException e) {
            botUser = userService.createNewUser(callerId);
        }
        return botUser.getVerificationCode();
    }
}
