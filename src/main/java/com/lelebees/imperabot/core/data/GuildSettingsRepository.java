package com.lelebees.imperabot.core.data;

import com.lelebees.imperabot.core.domain.GuildSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildSettingsRepository extends JpaRepository<GuildSettings, Long> {
}
