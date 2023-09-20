package com.lelebees.imperabot.bot.domain.user.exception;

public class UserNotInGameException extends RuntimeException {
    public UserNotInGameException(String message) {
        super(message);
    }
}
