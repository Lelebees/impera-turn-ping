package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

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
        User callingUser = event.getInteraction().getUser();
        Optional<ApplicationCommandInteractionOption> setOption = event.getOption("set");
        if (setOption.isPresent()) {
            UserNotificationSetting setting = UserNotificationSetting.get(Math.toIntExact(setOption.get()
                    .getOption("setting")
                    .orElseThrow(() -> new NullPointerException("You must specify a setting."))
                    .getValue().orElseThrow(() -> new NullPointerException("No setting entered"))
                    .asLong()));

            logger.info("User " + callingUser.getId().asLong() + " used /settings with setting: " + setting.ordinal() + " (" + setting + ")");

            BotUser user = userService.updateDefaultSetting(callingUser.getId().asLong(), setting);
            return event.reply().withEphemeral(true).withContent("Updated notification setting to `" + user.getNotificationSetting().toString() + "`");
        }

        BotUser botUser = userService.findOrCreateUser(callingUser.getId().asLong());

        logger.info("User " + callingUser.getId().asLong() + " used /settings view");

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("Settings for %s".formatted(callingUser.getUsername()))
                .addField("Default notification setting: ", "`%s`".formatted(botUser.getNotificationSetting().toString()), false)
                // TODO: Make this translation friendly
                .footer(botUser.isLinked() ? "Linked to Impera account: %s".formatted(botUser.getImperaId()) : "You are not linked to an Impera account", null)
                .color(Color.of(230, 200, 90)).build();

        return event.reply().withEphemeral(true).withEmbeds(embed);
    }
}
