package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.discord.application.LinkService;
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
    private final LinkService linkService;

    public UnlinkCommand(UserService userService, LinkService linkService) {
        this.userService = userService;
        this.linkService = linkService;
    }

    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        User user = event.getInteraction().getUser();
        logger.info("User " + user.getId().asLong() + " (" + user.getUsername() + ") used /unlink");
        try {
            linkService.unlinkUser(user.getId());
            return event.reply("Your discord account has been unlinked from your Impera account").withEphemeral(true);
        } catch (UserNotFoundException e) {
            return event.reply("We were unable to find your account").withEphemeral(true);
        } catch (Exception e) {
            logger.error("An unknown error occurred unlinking user ", e);
            return event.reply("An unknown error occurred while unlinking your account. Please try again later, or file a bug report.").withEphemeral(true);
        }
    }
}
