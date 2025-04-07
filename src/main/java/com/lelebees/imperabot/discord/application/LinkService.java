package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import discord4j.common.util.Snowflake;
import org.springframework.stereotype.Service;

@Service
public class LinkService {
    private final UserService userService;

    public LinkService(UserService userService) {
        this.userService = userService;
    }

    //TODO: put this in BotUser (they own this data, after all!)
    public String getVerificationCode(Snowflake id) throws UserAlreadyVerfiedException {
        BotUser user = userService.findOrCreateUser(id.asLong());
        if (user.isLinked()) {
            throw new UserAlreadyVerfiedException("User " + id.asLong() + " is already verified!");
        }
        return user.getVerificationCode();
    }

    public BotUser unlinkUser(Snowflake id) throws UserNotFoundException {
        return userService.unlinkUser(id.asLong());
    }
}
