package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.discord.application.LinkService;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
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
    @Value("${impera.username}")
    private String imperaUsername;
    private final LinkService linkService;
    private final DiscordService discordService;

    public LinkCommand(LinkService linkService, DiscordService discordService) {
        this.linkService = linkService;
        this.discordService = discordService;
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        try {
            User user = event.getInteraction().getUser();
            logger.info("User " + user.getId().asLong() + " (" + user.getUsername() + ") used /link");

            Long unlinkCommandId = discordService.getApplicationCommands().get("unlink");
            String verificationCode = linkService.getVerificationCode(user.getId());
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
            logger.info("User " + event.getInteraction().getUser().getId().asLong() + " (" + event.getInteraction().getUser().getUsername() + ") was denied access to /link because they are already verified");
            return event.reply("You are already verified! If you wish to re-link, run </unlink> first.").withEphemeral(true);
        } catch (Exception e) {
            logger.error("An unknown error occured: ", e);
            return event.reply("An unknown error occured. Please try again later.").withEphemeral(true);
        }
    }
}
