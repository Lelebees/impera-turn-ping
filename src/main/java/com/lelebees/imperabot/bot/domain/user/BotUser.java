package com.lelebees.imperabot.bot.domain.user;

import com.lelebees.imperabot.bot.data.converter.UserNotificationSettingsConverter;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.UUID;

import static com.lelebees.imperabot.bot.domain.user.UserNotificationSetting.PREFER_GUILD_OVER_DMS;

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
    @Convert(converter = UserNotificationSettingsConverter.class)
    public UserNotificationSetting notificationSetting;
    @Column(name = "super_secret_code")
    @Unique
    private String verificationCode;

    protected BotUser() {

    }

    public BotUser(long id) {
        this(id, null, PREFER_GUILD_OVER_DMS, null);
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

    @Nullable
    public UUID getImperaId() {
        return imperaId;
    }

    public boolean isLinked() {
        return this.imperaId != null;
    }

    public void setImperaId(UUID imperaId) throws UserAlreadyVerfiedException {
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

    public String getMention() {
        return "<@" + this.userId + ">";
    }
}
