package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.TranslationService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.translation.TranslationGroup;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SettingsCommand implements SlashCommand {

    private final static Logger logger = LoggerFactory.getLogger(SettingsCommand.class);
    private final UserService userService;
    private final TranslationService translationService;

    public SettingsCommand(UserService userService, TranslationService translationService) {
        this.userService = userService;
        this.translationService = translationService;
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        TranslationGroup translations = translationService.getTranslationsByGroup("settings");
        String locale = event.getInteraction().getUserLocale();
        if (event.getOption("set").isPresent()) {
            User callingUser = event.getInteraction().getUser();
            UserNotificationSetting setting = event.getOptions().get(0).getOption("setting")
                    .flatMap(option -> option.getValue()
                            .map(value -> UserNotificationSetting.get(Math.toIntExact(value.asLong())))
                    ).orElse(null);

            logger.info("User " + callingUser.getId().asLong() + " used /settings with setting: " + setting.ordinal() + " (" + setting + ")");

            BotUser user = userService.updateDefaultSetting(callingUser.getId().asLong(), setting);
            return event.reply().withEphemeral(true).withContent(translations.getTranslation("setNotification", locale).formatted(user.getNotificationSetting().toString()));
        }

        User user = event.getInteraction().getUser();
        BotUser botUser = userService.findOrCreateUser(user.getId().asLong());

        logger.info("User " + user.getId().asLong() + " used /settings view");

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title(translations.getTranslation("embedTitle", locale).formatted(user.getUsername()))
                .addField(translations.getTranslation("notificationSettingHeader", locale), translations.getTranslation("notificationSettingDescription", locale).formatted(botUser.getNotificationSetting().toString(locale)), false)
                .footer(botUser.isLinked() ? translations.getTranslation("linkedAccountFooterLinked", locale).formatted(botUser.getImperaId()) : translations.getTranslation("linkedAccountFooterNotLinked", locale), null)
                .color(Color.of(230, 200, 90)).build();

        return event.reply().withEphemeral(true).withEmbeds(embed);
    }
}
