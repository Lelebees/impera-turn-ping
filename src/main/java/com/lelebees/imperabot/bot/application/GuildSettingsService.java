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

    private GuildSettings getFromOptional(Optional<GuildSettings> optional) throws GuildSettingsNotFoundException {
        return optional.orElseThrow(() -> new GuildSettingsNotFoundException("Could not find guild settings"));
    }

    public GuildSettings getGuildSettingsById(long id) throws GuildSettingsNotFoundException {
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
        GuildSettings guildSettings = getGuildSettingsById(guildId);
        guildSettings.defaultChannelId = guildSettingsModificationDTO.channelId;
        guildSettings.permissionRoleId = guildSettingsModificationDTO.permissionRoleId;
        guildSettings.winnerRoleId = guildSettingsModificationDTO.winnerRoleId;
        return repository.save(guildSettings);
    }

    public GuildSettings getOrCreateGuildSettings(long guildId) {
        Optional<GuildSettings> settingsOptional = repository.findById(guildId);
        return settingsOptional.orElseGet(() -> createNewGuildSettings(guildId));
    }

    public boolean guildSettingsExist(long guildId) {
        return repository.existsById(guildId);
    }

    public GuildSettings updateDefaultChannel(long guildId, Long channelId) throws GuildSettingsNotFoundException {
        GuildSettings settings = getOrCreateGuildSettings(guildId);
        GuildSettingsModificationDTO guildSettingsModificationDTO = new GuildSettingsModificationDTO(settings);
        guildSettingsModificationDTO.channelId = channelId;
        return updateGuildSettings(guildId, guildSettingsModificationDTO);
    }

    public GuildSettings updatePermissionRole(long guildId, Long permissionRoleId) throws GuildSettingsNotFoundException {
        GuildSettings settings = getOrCreateGuildSettings(guildId);
        GuildSettingsModificationDTO guildSettingsModificationDTO = new GuildSettingsModificationDTO(settings);
        guildSettingsModificationDTO.permissionRoleId = permissionRoleId;
        return updateGuildSettings(guildId, guildSettingsModificationDTO);
    }

    public GuildSettings updateWinnerRole(long guildId, Long winnerRoleId) throws GuildSettingsNotFoundException {
        GuildSettings settings = getOrCreateGuildSettings(guildId);
        GuildSettingsModificationDTO guildSettingsModificationDTO = new GuildSettingsModificationDTO(settings);
        guildSettingsModificationDTO.winnerRoleId = winnerRoleId;
        return updateGuildSettings(guildId, guildSettingsModificationDTO);
    }
}
