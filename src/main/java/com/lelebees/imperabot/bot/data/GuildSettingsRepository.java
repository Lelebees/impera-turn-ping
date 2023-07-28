package com.lelebees.imperabot.bot.data;

import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildSettingsRepository extends JpaRepository<GuildSettings, Long> {
}
