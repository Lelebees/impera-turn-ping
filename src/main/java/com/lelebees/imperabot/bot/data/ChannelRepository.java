package com.lelebees.imperabot.bot.data;

import com.lelebees.imperabot.bot.domain.game.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
}
