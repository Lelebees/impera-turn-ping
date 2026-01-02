package com.lelebees.imperabot.user.domain;

import java.util.Arrays;

public enum UserNotificationSetting {
    NO_NOTIFICATIONS("No Notifications"),
    DMS_ONLY("DMs Only"),
    GUILD_ONLY("Guild Only"),
    PREFER_GUILD_OVER_DMS("Guild > DMs"),
    DMS_AND_GUILD("DMs & Guild");

    private final String value;

    UserNotificationSetting(final String value) {
        this.value = value;
    }

    public static UserNotificationSetting get(int settingOption) {
        return UserNotificationSetting.values()[settingOption];
    }

    public static UserNotificationSetting parse(String text) {
        return Arrays.stream(UserNotificationSetting.values())
                .filter(setting -> setting.value.equals(text))
                .toList()
                .get(0);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
