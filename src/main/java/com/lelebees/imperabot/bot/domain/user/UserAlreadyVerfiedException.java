package com.lelebees.imperabot.bot.domain.user;

public class UserAlreadyVerfiedException extends RuntimeException {
    public UserAlreadyVerfiedException(String message) {
        super(message);
    }
}
