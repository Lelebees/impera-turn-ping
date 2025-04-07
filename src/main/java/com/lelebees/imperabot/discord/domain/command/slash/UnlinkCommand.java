package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.Id;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UnlinkCommand implements SlashCommand {

    final UserService userService;

    public UnlinkCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Id id = event.getInteraction().getUser().getUserData().id();
        userService.unlinkUser(id.asLong());
        return event.reply().withEphemeral(true).withContent("Your discord account has been unlinked from your Impera account");
    }
}
