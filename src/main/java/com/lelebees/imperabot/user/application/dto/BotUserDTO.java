package com.lelebees.imperabot.user.application.dto;

import com.lelebees.imperabot.user.domain.BotUser;
import com.lelebees.imperabot.user.domain.UserNotificationSetting;
import discord4j.common.util.Snowflake;
import discord4j.rest.util.AllowedMentions;

import java.util.UUID;

public record BotUserDTO(long discordId, UUID imperaId, String username, UserNotificationSetting notificationSetting,
                         boolean isLinked) {

    public static BotUserDTO from(BotUser user) {
        return new BotUserDTO(user.getUserId(), user.getImperaId(), user.getUsername(), user.getNotificationSetting(), user.isLinked());
    }

    public String getMention() {
        return "<@" + this.discordId + ">";
    }

    public AllowedMentions getAllowedMentions() {
        return switch (notificationSetting()) {
            case NO_NOTIFICATIONS, DMS_ONLY -> AllowedMentions.suppressAll();
            case DMS_AND_GUILD, PREFER_GUILD_OVER_DMS, GUILD_ONLY ->
                    AllowedMentions.builder().allowUser(Snowflake.of(discordId())).build();
        };
    }
}
