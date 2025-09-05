package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.bot.presentation.user.BotUserDTO;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.*;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.lelebees.imperabot.bot.domain.user.UserNotificationSetting.*;

@Component
public class SettingsCommand implements SlashCommand {

    private final Logger logger = LoggerFactory.getLogger(SettingsCommand.class);
    private final UserService userService;
    private final Map<UserNotificationSetting, String> optionLabels = Map.of(
            NO_NOTIFICATIONS, "No notifications",
            DMS_ONLY, "Only in DMs",
            GUILD_ONLY, "Only in Servers",
            PREFER_GUILD_OVER_DMS, "Servers if available, otherwise, DM",
            DMS_AND_GUILD, "Servers and DMs"
    );


    public SettingsCommand(UserService userService, GatewayDiscordClient client) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        User callingUser = event.getInteraction().getUser();
        BotUserDTO botUser = userService.findOrCreateUser(callingUser.getId().asLong());

        return event.reply().withEphemeral(true).withComponents(
                Container.of(Color.of(230, 200, 90),
                        TextDisplay.of("# Settings for %s".formatted(callingUser.getUserData().globalName().get())),
                        TextDisplay.of("## :bell: Notifications"),
                        ActionRow.of(
                                SelectMenu.of("notification-setting", getOptions(botUser))
                        ),
                        TextDisplay.of("## :link: Impera Account"),
                        getImperaLinkComponent(botUser)

                )
        );
    }

    private Section getImperaLinkComponent(BotUserDTO botUser) {
        if (!botUser.isLinked()) {
            return Section.of(
                    Button.primary("link", "Link your account"),
                    TextDisplay.of("You're currently not linked to an Impera account."));
        }
        return Section.of(
                Button.danger("unlink", "Unlink your account"),
                TextDisplay.of("You're currently linked to: %s".formatted(botUser.username())),
                TextDisplay.of("-# (%s)".formatted(botUser.imperaId())));
    }

    private SelectMenu.Option[] getOptions(BotUserDTO botUser) {
        SelectMenu.Option[] options = new SelectMenu.Option[values().length];
        for (int i = 0; i < values().length; i++) {
            UserNotificationSetting setting = get(i);
            if (setting.equals(botUser.notificationSetting())) {
                options[i] = SelectMenu.Option.ofDefault(optionLabels.get(setting), setting.toString());
                continue;
            }
            options[i] = SelectMenu.Option.of(optionLabels.get(setting), setting.toString());
        }
        return options;
    }
}
