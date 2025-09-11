package com.lelebees.imperabot.bot.presentation.guildsettings;

import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;

import java.util.List;

public record GuildSettingsDTO(long guildId, Long defaultChannelId, Long permissionRoleId, Long winnerRoleId) {

    public static GuildSettingsDTO from(GuildSettings settings) {
        return new GuildSettingsDTO(settings.getId(), settings.defaultChannelId, settings.permissionRoleId, settings.winnerRoleId);
    }

    public boolean userHasEditPermission(User user) {
        Member member = user.asMember(Snowflake.of(guildId())).block();
        if (member == null) {
            return false;
        }
        boolean canManageChannelsAndRoles = member.getBasePermissions().block().containsAll(List.of(Permission.MANAGE_CHANNELS, Permission.MANAGE_ROLES));
        boolean hasPermissionRole = permissionRoleId() != null && member.getRoleIds().contains(Snowflake.of(permissionRoleId()));
        return canManageChannelsAndRoles || hasPermissionRole;
    }
}
