package com.lelebees.imperabot.bot.data.converter;

import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GuildNotificationSettingsConverter implements AttributeConverter<GuildNotificationSettings, Integer> {
    @Override
    public Integer convertToDatabaseColumn(GuildNotificationSettings guildNotificationSettings) {
        return guildNotificationSettings.ordinal();
    }

    @Override
    public GuildNotificationSettings convertToEntityAttribute(Integer integer) {
        return GuildNotificationSettings.values()[integer];
    }
}
