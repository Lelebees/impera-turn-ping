package com.lelebees.imperabot.bot.domain.guild.exception;

public class GuildSettingsNotFoundException extends RuntimeException {
    public GuildSettingsNotFoundException(String message) {
        super(message);
    }
}
