package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.TranslationService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.translation.TranslationGroup;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UnlinkCommand implements SlashCommand {

    private final UserService userService;
    private final TranslationService translationService;

    public UnlinkCommand(UserService userService, TranslationService translationService) {
        this.userService = userService;
        this.translationService = translationService;
    }

    @Override
    public String getName() {
        return "unlink";
    }

    //TODO: proper error handling
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        TranslationGroup translations = translationService.getTranslationsByGroup("unlink");
        Snowflake id = event.getInteraction().getUser().getId();
        String locale = event.getInteraction().getUserLocale();
        userService.unlinkUser(id.asLong());
        return event.reply().withEphemeral(true).withContent(translations.getTranslation("success", locale));
    }
}
