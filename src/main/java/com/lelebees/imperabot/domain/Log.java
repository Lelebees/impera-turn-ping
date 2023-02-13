package com.lelebees.imperabot.domain;

import java.time.LocalDateTime;

public class Log {
    private long id;
    private User bot_user;
    private LocalDateTime timestamp;
    private String description;

    //For hibernate
    public Log() {
    }

    public Log(long id, LocalDateTime timestamp, String description) {
        this.id = id;
        this.timestamp = timestamp;
        this.description = description;
    }

    public Log(long id, User bot_user, LocalDateTime timestamp, String description) {
        this(id, timestamp, description);
        this.bot_user = bot_user;
    }
}
