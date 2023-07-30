package com.lelebees.imperabot.bot.domain.guild;

public enum GuildNotificationSettings {
    NO_NOTIFICATIONS("No Notifications"),
    NOTIFICATIONS_ON("Notifications On");

    private final String textForm;

    GuildNotificationSettings(final String textForm) {
        this.textForm = textForm;
    }

    @Override
    public String toString() {
        return textForm;
    }
}
