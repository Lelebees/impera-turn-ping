package com.lelebees.imperabot.core.application.dto;

import com.lelebees.imperabot.core.domain.GuildSettings;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;

import java.time.Duration;

public record GuildSettingsDTO(Snowflake guildId, ChannelDTO defaultChannel, Snowflake permissionRoleId,
                               Snowflake winnerRoleId) {

    public static GuildSettingsDTO from(GuildSettings settings) {
        return new GuildSettingsDTO(
                Snowflake.of(settings.getId()),
                settings.defaultChannel == null ? null : ChannelDTO.From(settings.defaultChannel),
                settings.permissionRoleId == null ? null : Snowflake.of(settings.permissionRoleId),
                settings.winnerRoleId == null ? null : Snowflake.of(settings.winnerRoleId)
        );
    }

    public boolean hasDefaultChannelPermissions(User user) {
        Member member = user.asMember(guildId).block();
        if (member == null) {
            return false;
        }
        boolean canManageChannels = member.getBasePermissions().block(Duration.ofSeconds(5)).contains(Permission.MANAGE_CHANNELS);
        return canManageChannels || hasPermissionRole(user);
    }

    public boolean hasTrackPermissions(User user) {
        return hasDefaultChannelPermissions(user);
    }

    public boolean hasVanityRoleManagePermissions(User user) {
        Member member = user.asMember(guildId).block();
        if (member == null) {
            return false;
        }
        boolean canManageVanityRole = member.getBasePermissions().block(Duration.ofSeconds(5)).contains(Permission.MANAGE_ROLES);
        return canManageVanityRole || hasPermissionRole(user);
    }

    public boolean hasPermissionRoleManagePermissions(User user) {
        Member member = user.asMember(guildId).block();
        if (member == null) {
            return false;
        }
        return member.getBasePermissions().block(Duration.ofSeconds(5)).contains(Permission.MANAGE_ROLES);
    }

    private boolean hasPermissionRole(User user) {
        Member member = user.asMember(guildId).block();
        if (member == null) {
            return false;
        }
        return permissionRoleId() != null && member.getRoleIds().contains(permissionRoleId);
    }
}
