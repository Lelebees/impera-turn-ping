package com.lelebees.imperabot.bot.data;

import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameLinkId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameChannelLinkRepository extends JpaRepository<GameChannelLink, GameLinkId> {

    List<GameChannelLink> findGameChannelLinkByChannelId(long channelId);

    List<GameChannelLink> findGameChannelLinkByGameId(long gameId);
}
