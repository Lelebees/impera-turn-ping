package com.lelebees.imperabot.bot.domain.log;

import com.lelebees.imperabot.bot.domain.user.BotUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bot_log")
public class Log {
    @Id
    private long id;
    @OneToOne(optional = true)
    private BotUser bot_user;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    @Column(name = "description")
    private String description;

    //For hibernate
    protected Log() {
    }

    public Log(long id, LocalDateTime timestamp, String description) {
        this.id = id;
        this.timestamp = timestamp;
        this.description = description;
    }

    public Log(long id, BotUser bot_user, LocalDateTime timestamp, String description) {
        this(id, timestamp, description);
        this.bot_user = bot_user;
    }
}
