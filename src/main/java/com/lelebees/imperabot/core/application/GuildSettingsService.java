package com.lelebees.imperabot.core.application;

import com.lelebees.imperabot.core.application.dto.GuildSettingsDTO;
import com.lelebees.imperabot.core.application.exception.ChannelNotFoundException;
import com.lelebees.imperabot.core.application.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.core.data.GuildSettingsRepository;
import com.lelebees.imperabot.core.domain.Channel;
import com.lelebees.imperabot.core.domain.GuildSettings;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GuildSettingsService {

    private final GuildSettingsRepository repository;
    private final ChannelService channelService;

    public GuildSettingsService(GuildSettingsRepository repository, ChannelService channelService) {
        this.repository = repository;
        this.channelService = channelService;
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

    public GuildSettingsDTO createNewGuildSettings(long guildId) {
        return GuildSettingsDTO.from(repository.save(GuildSettings.From(guildId)));
    }

    public GuildSettingsDTO updateDefaultChannel(long guildId, Long channelId) throws GuildSettingsNotFoundException {
        GuildSettings settings = findGuildSettings(guildId);
        if (channelId == null) {
            settings.defaultChannel = null;
        } else {
            Channel channel;
            try {
                channel = channelService.findChannel(channelId);
            } catch (ChannelNotFoundException e) {
                channel = channelService.createChannel(channelId);
            }
            settings.defaultChannel = channel;
        }
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
