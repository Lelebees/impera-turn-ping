package com.lelebees.imperabot.domain.bot;

import com.lelebees.imperabot.domain.bot.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import java.time.LocalDateTime;
@Entity
public class Log {
    @Id
    private long id;
    @OneToOne(optional = true)
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
