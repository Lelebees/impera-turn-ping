package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
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

    public SettingsCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        if (event.getOption("set").isPresent()) {
            User callingUser = event.getInteraction().getUser();
            UserNotificationSetting setting = event.getOptions().get(0).getOption("setting").flatMap(option -> option.getValue().map(value -> UserNotificationSetting.get(Math.toIntExact(value.asLong())))).orElse(null);

            logger.info("User " + callingUser.getId().asLong() + " used /settings with setting: " + setting.ordinal() + " (" + setting + ")");

            BotUser user = userService.updateDefaultSetting(callingUser.getId().asLong(), setting);
            return event.reply().withEphemeral(true).withContent("Updated notification setting to `" + user.getNotificationSetting().toString() + "`");
        }

        User user = event.getInteraction().getUser();
        BotUser botUser = userService.findOrCreateUser(user.getId().asLong());

        logger.info("User " + user.getId().asLong() + " used /settings view");

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("Settings for %s".formatted(user.getUsername()))
                .addField("Default notification setting: ", "`%s`".formatted(botUser.getNotificationSetting().toString()), false)
                .footer(botUser.isLinked() ? "Linked to Impera account: %s".formatted(botUser.getImperaId()) : "You are not linked to an Impera account", null)
                .color(Color.of(230, 200, 90)).build();

        return event.reply().withEphemeral(true).withEmbeds(embed);
    }
}
