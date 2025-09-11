package com.lelebees.imperabot.discord.domain.command.button;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.bot.presentation.user.BotUserDTO;
import com.lelebees.imperabot.discord.domain.SettingsMenu;
import com.lelebees.imperabot.discord.domain.command.ButtonCommand;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class UnlinkButton implements ButtonCommand {
    private final Logger logger = LoggerFactory.getLogger(UnlinkButton.class);


    private final UserService userService;

    public UnlinkButton(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getCustomId() {
        return "unlink";
    }

    @Override
    public Mono<Void> handle(ButtonInteractionEvent event) {
        User user = event.getInteraction().getUser();
        try {
            BotUserDTO botUser = userService.unlinkUser(user.getId().asLong());
            return event.edit(InteractionApplicationCommandCallbackSpec.builder().addAllComponents(List.of(SettingsMenu.buildForUser(botUser, user))).build());
        } catch (UserNotFoundException e) {
            return event.reply("We were unable to find your account").withEphemeral(true);
        } catch (RuntimeException e) {
            logger.error("An unknown error occurred unlinking user ", e);
            return event.reply("An unknown error occurred while unlinking your account. Please try again later, or file a bug report.").withEphemeral(true);
        }
    }
}
