package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UnlinkCommand implements SlashCommand {
    private static final Logger logger = LoggerFactory.getLogger(UnlinkCommand.class);

    private final UserService userService;

    public UnlinkCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        User user = event.getInteraction().getUser();
        try {
            userService.unlinkUser(user.getId().asLong());
            return event.reply("Your discord account has been unlinked from your Impera account").withEphemeral(true);
        } catch (UserNotFoundException e) {
            return event.reply("We were unable to find your account").withEphemeral(true);
        } catch (RuntimeException e) {
            logger.error("An unknown error occurred unlinking user ", e);
            return event.reply("An unknown error occurred while unlinking your account. Please try again later, or file a bug report.").withEphemeral(true);
        }
    }
}
