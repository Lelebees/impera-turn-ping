package com.lelebees.imperabot.discord.domain.command;

import com.lelebees.imperabot.ImperaBotApplication;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LinkCommand implements SlashCommand {

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        //TODO: Make a new user if one hasn't been found already, and do shtuff.
        return event.reply()
                .withEphemeral(true)
                .withContent("""
                        Impera Auto Ping has received your link request! Here's what we need you to do:
                        > 1. Go to https://imperaonline.de and log in
                        > 2. Click on the messages icon and click Compose
                        > 3. Enter "%s" in the "User" field
                        > 4. Enter "Link Request" in the subject field.
                        > 5. Enter the following code into the "Text" field: ||%s||
                        > :warning: **IMPORTANT**: DO ***NOT*** SHARE THIS CODE WITH ANYONE!
                        > 6. Press send!
                        After completing these steps, we'll know it's you, and you will be linked! You can unlink at any time by using the "/unlink" command!"""
                        .formatted(ImperaBotApplication.env.get("IMPERA_USER_NAME"), "CODE_PLACEHOLDER"));
    }
}
