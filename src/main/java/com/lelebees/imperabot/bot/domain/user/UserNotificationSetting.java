package com.lelebees.imperabot.bot.domain.user;

import com.lelebees.imperabot.bot.domain.NotificationSettings;

public enum UserNotificationSetting implements NotificationSettings {
    NO_NOTIFICATIONS("No Notifications"),
    DMS_ONLY("DMs Only"),
    GUILD_ONLY("Guild Only"),
    PREFER_GUILD_OVER_DMS("Guild > DMs"),
    DMS_AND_GUILD("DMs & Guild");

    private final String textForm;

    UserNotificationSetting(final String textForm) {
        this.textForm = textForm;
    }

    @Override
    public String toString() {
        return this.textForm;
    }
}
