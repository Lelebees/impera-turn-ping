package com.lelebees.imperabot.bot.data;

import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameLinkId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameChannelLinkRepository extends JpaRepository<GameChannelLink, GameLinkId> {
}
