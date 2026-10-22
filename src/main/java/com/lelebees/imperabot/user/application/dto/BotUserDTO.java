package com.lelebees.imperabot.user.application.dto;

import com.lelebees.imperabot.user.domain.BotUser;
import com.lelebees.imperabot.user.domain.UserNotificationSetting;
import discord4j.common.util.Snowflake;
import discord4j.rest.util.AllowedMentions;

import java.util.UUID;

public record BotUserDTO(Snowflake id, UUID imperaId, String username, UserNotificationSetting notificationSetting,
                         boolean isLinked) {

    public static BotUserDTO from(BotUser user) {
        return new BotUserDTO(Snowflake.of(user.getUserId()), user.getImperaId(), user.getUsername(), user.getNotificationSetting(), user.isLinked());
    }

    public String getMention() {
        return "<@" + this.id.asString() + ">";
    }

    public AllowedMentions getAllowedMentions() {
        return switch (notificationSetting()) {
            case NO_NOTIFICATIONS, DMS_ONLY -> AllowedMentions.suppressAll();
            case DMS_AND_GUILD, PREFER_GUILD_OVER_DMS, GUILD_ONLY ->
                    AllowedMentions.builder().allowUser(id()).build();
        };
    }

    public long discordId() {
        return id.asLong();
    }
}
