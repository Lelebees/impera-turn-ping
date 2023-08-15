package com.lelebees.imperabot.bot.data.converter;

import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserNotificationSettingsConverter implements AttributeConverter<UserNotificationSetting, Integer> {
    @Override
    public Integer convertToDatabaseColumn(UserNotificationSetting userNotificationSetting) {
        return userNotificationSetting.ordinal();
    }

    @Override
    public UserNotificationSetting convertToEntityAttribute(Integer integer) {
        return UserNotificationSetting.values()[integer];
    }
}
