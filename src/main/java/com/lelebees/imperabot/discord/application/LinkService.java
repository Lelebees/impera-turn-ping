package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import discord4j.common.util.Snowflake;
import org.springframework.stereotype.Service;

@Service
public class LinkService {
    private final UserService userService;

    public LinkService(UserService userService) {
        this.userService = userService;
    }

    public String getVerificationCode(Snowflake id) {
        BotUser user = userService.findOrCreateUser(id.asLong());
        if (user.isLinked()) {
            throw new UserAlreadyVerfiedException("User " + id.asLong() + " is already verified!");
        }
        return user.getVerificationCode();
    }

    public BotUser unlinkUser(Snowflake id) {
        return userService.unlinkUser(id.asLong());
    }
}
