package com.lelebees.imperabot.discord.domain.command.button;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.discord.domain.command.ButtonCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LinkButton implements ButtonCommand {
    private final Logger logger = LoggerFactory.getLogger(LinkButton.class);
    private final UserService userService;
    private final DiscordService discordService;
    @Value("${impera.username}")
    private String imperaUsername;

    public LinkButton(UserService userService, DiscordService discordService) {
        this.userService = userService;
        this.discordService = discordService;
    }

    @Override
    public String getCustomId() {
        return "link";
    }

    @Override
    public Mono<Void> handle(ButtonInteractionEvent event) {
        User user = event.getInteraction().getUser();
        Snowflake id = user.getId();
        String username = user.getUsername();
        Long settingsCommandId = discordService.getApplicationCommands().get("settings");
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
                            After completing these steps, we'll know it's you, and you will be linked! You can unlink at any time by clicking the button in </settings:%d>!"""
                            .formatted(imperaUsername, verificationCode, settingsCommandId))
                    .withComponents(ActionRow.of(button));
        } catch (UserAlreadyVerfiedException e) {
            logger.info("User " + username + " (" + id.asLong() + ") was unable to link because they are already verified");
            return event.reply("You are already verified! If you wish to re-link, press the button in </settings:%d> first.".formatted(settingsCommandId)).withEphemeral(true);
        } catch (RuntimeException e) {
            logger.error("An unknown error occurred: ", e);
            return event.reply("An unknown error occurred. Please try again later, or file a bug report.").withEphemeral(true);
        }
    }
}
