package com.lelebees.imperabot.discord.domain.command;

import com.lelebees.imperabot.ImperaBotApplication;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.Id;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LinkCommand implements SlashCommand {

    final UserService userService;

    public LinkCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Id id = event.getInteraction().getMember().orElseThrow(() -> new NullPointerException("No user made this request?!")).getMemberData().user().id();

        BotUser user = userService.findOrCreateUser(id.asLong());
        if (user.isLinked()) {
            return event.reply().withEphemeral(true).withContent("You are already linked to an Impera account!");
        }
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
                        .formatted(ImperaBotApplication.env.get("IMPERA_USER_NAME"), user.getVerificationCode()));
    }
}
