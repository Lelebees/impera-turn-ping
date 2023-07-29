package com.lelebees.imperabot.bot.domain.user;

import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.UUID;

import static com.lelebees.imperabot.bot.domain.user.UserNotificationSetting.NO_NOTIFICATIONS;

@Entity
@Table(name = "bot_user")
public class BotUser {
    @Id
    @Column(name = "discord_user_id")
    // This value is gained through discord, and is thus not generated.
    private long userId;
    @Column(name = "impera_player_id")
    @Unique
    @Nullable
    private UUID imperaId;
    @Column(name = "notification_setting")
    //Probably want to turn this into an enum instead!
    private UserNotificationSetting notificationSetting;
    @Column(name = "super_secret_code")
    @Unique
    private String verificationCode;

    protected BotUser() {

    }

    public BotUser(long id) {
        this(id, null, NO_NOTIFICATIONS, "");
        this.verificationCode = generateVerificationCode();
    }

    public BotUser(long userId, @Nullable @Unique UUID imperaId, UserNotificationSetting notificationSetting, @Unique String verificationCode) {
        this.userId = userId;
        this.imperaId = imperaId;
        this.notificationSetting = notificationSetting;
        this.verificationCode = verificationCode;
    }

    public long getUserId() {
        return userId;
    }

    public UUID getImperaId() {
        return imperaId;
    }

    public boolean isLinked() {
        return this.imperaId != null;
    }

    public void setImperaId(UUID imperaId) {
        if (this.imperaId != null) {
            throw new UserAlreadyVerfiedException("This user [" + this.userId + "] is already linked to an Impera account [" + this.imperaId + "] !");
        }
        this.imperaId = imperaId;
    }

    // Is this good practice? Lol no.
    // But it does work, so I cant be bothered to change it.
    private String generateVerificationCode() {
        return UUID.randomUUID().toString();
    }

    public UserNotificationSetting getNotificationSetting() {
        return notificationSetting;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void unlink() {
        this.imperaId = null;
        this.verificationCode = generateVerificationCode();
    }
}