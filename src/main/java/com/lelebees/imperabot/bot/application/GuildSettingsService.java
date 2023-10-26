package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GuildSettingsRepository;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.bot.domain.guild.exception.GuildSettingsNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GuildSettingsService {

    private final GuildSettingsRepository repository;

    public GuildSettingsService(GuildSettingsRepository repository) {
        this.repository = repository;
    }

    private GuildSettings getFromOptional(Optional<GuildSettings> optional) {
        return optional.orElseThrow(() -> new GuildSettingsNotFoundException("Could not find guild settings"));
    }

    public GuildSettings getGuildSettingsById(long id) {
        return getFromOptional(repository.findById(id));
    }

    public GuildSettings createNewGuildSettings(long guildId) {
        return repository.save(new GuildSettings(guildId));
    }

    public GuildSettings updateGuildSettings(long guildId, Long channelId, GuildNotificationSettings notificationSetting) {
        GuildSettings guildSettings = getGuildSettingsById(guildId);
        guildSettings.defaultChannelId = channelId;
        if (notificationSetting != null) {
            guildSettings.notificationSetting = notificationSetting;
        }
        return repository.save(guildSettings);
    }

    public GuildSettings getOrCreateGuildSettings(long guildId) {
        Optional<GuildSettings> settingsOptional = repository.findById(guildId);
        return settingsOptional.orElseGet(() -> createNewGuildSettings(guildId));
    }

    public boolean guildSettingsExist(long guildId) {
        return repository.existsById(guildId);
    }

    public void updateDefaultChannel(long guildId, long channelId) {
        GuildSettings settings = getOrCreateGuildSettings(guildId);
        settings.defaultChannelId = channelId;
        repository.save(settings);
    }
}
