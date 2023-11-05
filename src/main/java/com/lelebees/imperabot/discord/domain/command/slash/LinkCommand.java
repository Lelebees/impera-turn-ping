package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.TranslationService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.translation.TranslationGroup;
import com.lelebees.imperabot.bot.domain.user.BotUser;
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
    @Value("${impera.username}")
    private String imperaUsername;
    private final UserService userService;
    private final DiscordService discordService;
    private final TranslationService translationService;

    public LinkCommand(UserService userService, DiscordService discordService, TranslationService translationService) {
        this.userService = userService;
        this.discordService = discordService;
        this.translationService = translationService;
    }

    @Override
    public String getName() {
        return "link";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String locale = event.getInteraction().getUserLocale();
        TranslationGroup translations = translationService.getTranslationsByGroup("link");
        User callingUser = event.getInteraction().getUser();
        Snowflake id = callingUser.getId();

        logger.info("User " + id.asLong() + " (" + event.getInteraction().getUser().getUsername() + ") used /link");
        Long unlinkCommandId = discordService.getApplicationCommands().get("unlink");
        BotUser user = userService.findOrCreateUser(id.asLong());
        if (user.isLinked()) {
            logger.info("User " + id.asLong() + " (" + event.getInteraction().getUser().getUsername() + ") was denied access to /link because they are already linked");
            return event.reply().withEphemeral(true).withContent(translations.getTranslation("userIsLinked", locale).formatted(unlinkCommandId));
        }
        Button button = Button.primary("mobileCode", translations.getTranslation("mobileCodeButton", locale));
        return event.reply()
                .withEphemeral(true)
                .withContent(translations.getTranslation("instructions", locale).formatted(imperaUsername, user.getVerificationCode(), unlinkCommandId))
                .withComponents(ActionRow.of(button));
    }
}
