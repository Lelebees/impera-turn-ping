package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.discord.domain.SettingsMenu;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import com.lelebees.imperabot.user.application.UserService;
import com.lelebees.imperabot.user.application.dto.BotUserDTO;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SettingsCommand implements SlashCommand {

    private final Logger logger = LoggerFactory.getLogger(SettingsCommand.class);
    private final UserService userService;


    public SettingsCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        User callingUser = event.getInteraction().getUser();
        BotUserDTO botUser = userService.findOrCreateUser(callingUser.getId().asLong());

        return event.reply().withEphemeral(true).withComponents(SettingsMenu.buildForUser(botUser, callingUser));
    }
}
