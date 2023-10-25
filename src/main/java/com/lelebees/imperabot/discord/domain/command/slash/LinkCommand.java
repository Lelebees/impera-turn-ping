package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LinkCommand implements SlashCommand {

    final UserService userService;
    @Value("${impera.username}")
    private String imperaUsername;

    public LinkCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Snowflake id = event.getInteraction().getUser().getId();

        BotUser user = userService.findOrCreateUser(id.asLong());
        if (user.isLinked()) {
            // TODO: link the unlink command
            return event.reply().withEphemeral(true).withContent("You are already linked to an Impera account. If you wish to re-link, run \"/unlink\" first.");
        }
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
                        After completing these steps, we'll know it's you, and you will be linked! You can unlink at any time by using the "/unlink" command!"""
                        .formatted(imperaUsername, user.getVerificationCode()))
                .withComponents(ActionRow.of(button));
    }
}
