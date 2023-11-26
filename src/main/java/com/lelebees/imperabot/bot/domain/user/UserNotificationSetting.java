package com.lelebees.imperabot.bot.domain.user;

import com.lelebees.imperabot.bot.domain.NotificationSettings;

public enum UserNotificationSetting implements NotificationSettings {
    NO_NOTIFICATIONS("No Notifications", "Keine Benachrichtigungen"),
    DMS_ONLY("DMs Only", "Nur PNs"),
    GUILD_ONLY("Guild Only", "Nur Server"),
    PREFER_GUILD_OVER_DMS("Guild > DMs", "Server > PNs"),
    DMS_AND_GUILD("DMs & Guild", "PNs & Server");

    private final String value;
    private final String germanValue;

    UserNotificationSetting(final String value, final String germanValue) {
        this.value = value;
        this.germanValue = germanValue;
    }

    public static UserNotificationSetting get(int settingOption) {
        return UserNotificationSetting.values()[settingOption];
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String toString(String locale) {
        if (locale.equals("de")) {
            return this.germanValue;
        }
        return this.value;
    }
}
