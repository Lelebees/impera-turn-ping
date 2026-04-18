package com.lelebees.imperabot.bot.application.runnable;

public class DamageControlException extends RuntimeException {
    public DamageControlException() {
    }

    public DamageControlException(String message) {
        super(message);
    }

    public DamageControlException(String message, Throwable cause) {
        super(message, cause);
    }

    public DamageControlException(Throwable cause) {
        super(cause);
    }

    public DamageControlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
