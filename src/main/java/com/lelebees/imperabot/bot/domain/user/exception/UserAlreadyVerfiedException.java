package com.lelebees.imperabot.bot.domain.user.exception;

public class UserAlreadyVerfiedException extends RuntimeException {
    public UserAlreadyVerfiedException(String message) {
        super(message);
    }
}
