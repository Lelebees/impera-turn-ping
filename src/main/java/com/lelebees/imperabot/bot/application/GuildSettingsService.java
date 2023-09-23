package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GuildSettingsRepository;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.bot.domain.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsModificationDTO;
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

    public GuildSettings updateGuildSettings(long guildId, GuildSettingsModificationDTO dto) {
        GuildSettings guildSettings = getGuildSettingsById(guildId);
        guildSettings.defaultChannelId = dto.channelId;
        guildSettings.notificationSetting = dto.notificationSetting;
        return repository.save(guildSettings);
    }

    public GuildSettings getOrCreateGuildSettings(long guildId) {
        Optional<GuildSettings> settingsOptional = repository.findById(guildId);
        return settingsOptional.orElseGet(() -> createNewGuildSettings(guildId));
    }

    public boolean guildSettingsExist(long guildId) {
        return repository.existsById(guildId);
    }
}
