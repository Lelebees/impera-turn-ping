package com.lelebees.imperabot.bot.data;

import com.lelebees.imperabot.bot.domain.user.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<BotUser, Long> {

    Optional<BotUser> getUserByImperaId(UUID imperaId);

    Optional<BotUser> getUserByVerificationCode(String verificationCode);
}
