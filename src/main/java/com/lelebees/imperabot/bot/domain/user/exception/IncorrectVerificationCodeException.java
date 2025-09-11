package com.lelebees.imperabot.bot.domain.user.exception;

public class IncorrectVerificationCodeException extends Exception {
    public IncorrectVerificationCodeException(String message) {
        super(message);
    }
}
