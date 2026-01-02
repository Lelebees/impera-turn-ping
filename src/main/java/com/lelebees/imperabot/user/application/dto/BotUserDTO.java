package com.lelebees.imperabot.user.application.dto;

import com.lelebees.imperabot.user.domain.BotUser;
import com.lelebees.imperabot.user.domain.UserNotificationSetting;

import java.util.UUID;

public record BotUserDTO(long discordId, UUID imperaId, String username, UserNotificationSetting notificationSetting,
                         boolean isLinked) {

    public static BotUserDTO from(BotUser user) {
        return new BotUserDTO(user.getUserId(), user.getImperaId(), user.getUsername(), user.getNotificationSetting(), user.isLinked());
    }

    public String getMention() {
        return "<@" + this.discordId + ">";
    }
}
