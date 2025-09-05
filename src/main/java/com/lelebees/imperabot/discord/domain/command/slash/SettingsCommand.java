package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.*;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class SettingsCommand implements SlashCommand {

    private final Logger logger = LoggerFactory.getLogger(SettingsCommand.class);
    private final UserService userService;
    private final SelectMenu.Option[] options = {
            SelectMenu.Option.of("No notifications", "No Notifications"),
            SelectMenu.Option.of("Only in DMs", "DMs only"),
            SelectMenu.Option.of("Only in Servers", "Guild Only"),
            SelectMenu.Option.of("Servers if available, otherwise, DM", "Guild > DMs"),
            SelectMenu.Option.of("Servers and DMs", "DMs & Guild")
    };

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
        // TODO: Replace with DTO
        BotUser botUser = userService.findOrCreateUser(callingUser.getId().asLong());

        logger.info("User " + callingUser.getId().asLong() + " used /settings view");

        logger.info(String.valueOf(options[botUser.getNotificationSetting().ordinal()].isDefault()));
        //TODO: fix the placeholder - i'd like it to just have something selected
        return event.reply().withEphemeral(true).withComponents(
                Container.of(Color.of(230, 200, 90),
                        TextDisplay.of("# Settings for %s".formatted(callingUser.getUserData().globalName().get())),
                        TextDisplay.of("## :bell: Notifications"),
                        ActionRow.of(
                                SelectMenu.of("notification-setting", options)
                                        .withPlaceholder("Currently: " + botUser.getNotificationSetting().toString())
                        ),
                        TextDisplay.of("## :link: Impera Account"),
                        getImperaLinkComponent(botUser)

                )
        );
    }

    private Section getImperaLinkComponent(BotUser botUser) {
        if (!botUser.isLinked()) {
            return Section.of(
                    Button.primary("link", "Link your account"),
                    TextDisplay.of("You're currently not linked to an Impera account."));
        }
        return Section.of(
                Button.danger("unlink", "Unlink your account"),
                TextDisplay.of("You're currently linked to: " + botUser.getUsername()),
                TextDisplay.of("-# (%s)".formatted(botUser.getImperaId())));
    }
}
