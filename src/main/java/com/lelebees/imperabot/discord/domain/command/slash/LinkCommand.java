package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LinkCommand implements SlashCommand {

    private final static Logger logger = LoggerFactory.getLogger(LinkCommand.class);
    private final UserService userService;
    private final DiscordService discordService;
    @Value("${impera.username}")
    private String imperaUsername;

    public LinkCommand(UserService userService, DiscordService discordService) {
        this.userService = userService;
        this.discordService = discordService;
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        User user = event.getInteraction().getUser();
        Snowflake id = user.getId();
        String username = user.getUsername();
        Long unlinkCommandId = discordService.getApplicationCommands().get("unlink");
        try {
            String verificationCode = userService.startVerification(id.asLong());
            Button button = Button.primary("mobileCode", "Send me a mobile friendly code!");
            return event.reply()
                    .withEphemeral(true)
                    .withContent("""
                            Impera Auto Ping has received your link request! Here's what we need you to do:
                            > 1. Go to https://imperaonline.de and log in
                            > 2. Click on the messages icon and click Compose
                            > 3. Enter "%s" in the "User" field
                            > 4. Enter "link" in the subject field.
                            > 5. Enter the following code into the "Text" field: ||```%s```||
                            > :warning: **IMPORTANT**: DO ***NOT*** SHARE THIS CODE WITH ANYONE!
                            > 6. Press send!
                            After completing these steps, we'll know it's you, and you will be linked! You can unlink at any time by using the </unlink:%s> command!"""
                            .formatted(imperaUsername, verificationCode, unlinkCommandId))
                    .withComponents(ActionRow.of(button));
        } catch (UserAlreadyVerfiedException e) {
            logger.info("User " + id.asLong() + " (" + username + ") was denied access to /link because they are already verified");
            return event.reply("You are already verified! If you wish to re-link, run </unlink:" + unlinkCommandId + "> first.").withEphemeral(true);
        } catch (RuntimeException e) {
            logger.error("An unknown error occured: ", e);
            return event.reply("An unknown error occured. Please try again later, or file a bug report.").withEphemeral(true);
        }
    }
}
