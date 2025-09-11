package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GuildSettingsRepository;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.bot.domain.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsDTO;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsModificationDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GuildSettingsService {

    private final GuildSettingsRepository repository;

    public GuildSettingsService(GuildSettingsRepository repository) {
        this.repository = repository;
    }

    private GuildSettings getFromOptional(Optional<GuildSettings> optional) throws GuildSettingsNotFoundException {
        return optional.orElseThrow(() -> new GuildSettingsNotFoundException("Could not find guild settings"));
    }

    public GuildSettingsDTO getGuildSettingsById(long id) throws GuildSettingsNotFoundException {
        return GuildSettingsDTO.from(findGuildSettings(id));
    }

    private GuildSettings findGuildSettings(long id) throws GuildSettingsNotFoundException {
        try {
            return getFromOptional(repository.findById(id));
        } catch (GuildSettingsNotFoundException e) {
            throw new GuildSettingsNotFoundException("Could not find settings for guild: " + id);
        }
    }

    public GuildSettings createNewGuildSettings(long guildId) {
        return repository.save(new GuildSettings(guildId));
    }

    public GuildSettings updateGuildSettings(long guildId, GuildSettingsModificationDTO guildSettingsModificationDTO) throws GuildSettingsNotFoundException {
        GuildSettings guildSettings = findGuildSettings(guildId);
        guildSettings.defaultChannelId = guildSettingsModificationDTO.channelId;
        guildSettings.permissionRoleId = guildSettingsModificationDTO.permissionRoleId;
        guildSettings.winnerRoleId = guildSettingsModificationDTO.winnerRoleId;
        return repository.save(guildSettings);
    }

    public GuildSettingsDTO getOrCreateGuildSettings(long guildId) {
        Optional<GuildSettings> settingsOptional = repository.findById(guildId);
        return GuildSettingsDTO.from(settingsOptional.orElseGet(() -> createNewGuildSettings(guildId)));
    }

    public boolean guildSettingsExist(long guildId) {
        return repository.existsById(guildId);
    }

    public GuildSettingsDTO updateDefaultChannel(long guildId, Long channelId) throws GuildSettingsNotFoundException {
        GuildSettings settings = findGuildSettings(guildId);
        settings.defaultChannelId = channelId;
        return GuildSettingsDTO.from(repository.save(settings));
    }

    public GuildSettingsDTO updatePermissionRole(long guildId, Long permissionRoleId) throws GuildSettingsNotFoundException {
        GuildSettings settings = findGuildSettings(guildId);
        settings.permissionRoleId = permissionRoleId;
        return GuildSettingsDTO.from(repository.save(settings));
    }

    public GuildSettingsDTO updateWinnerRole(long guildId, Long winnerRoleId) throws GuildSettingsNotFoundException {
        GuildSettings settings = findGuildSettings(guildId);
        settings.winnerRoleId = winnerRoleId;
        return GuildSettingsDTO.from(repository.save(settings));
    }
}
