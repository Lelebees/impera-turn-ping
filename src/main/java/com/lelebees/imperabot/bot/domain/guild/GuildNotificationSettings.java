package com.lelebees.imperabot.bot.domain.guild;

import com.lelebees.imperabot.bot.domain.NotificationSettings;

public enum GuildNotificationSettings implements NotificationSettings {
    NO_NOTIFICATIONS("No Notifications"),
    NOTIFICATIONS_ON("Notifications On");

    private final String value;

    GuildNotificationSettings(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
