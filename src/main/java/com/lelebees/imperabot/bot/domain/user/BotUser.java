package com.lelebees.imperabot.bot.domain.user;

import com.lelebees.imperabot.bot.data.converter.UserNotificationSettingsConverter;
import com.lelebees.imperabot.bot.domain.user.exception.IncorrectVerificationCodeException;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.UUID;

import static com.lelebees.imperabot.bot.domain.user.UserNotificationSetting.PREFER_GUILD_OVER_DMS;

@Entity
@Table(name = "bot_user")
public class BotUser {
    @Column(name = "notification_setting")
    @Convert(converter = UserNotificationSettingsConverter.class)
    public UserNotificationSetting notificationSetting;
    @Id
    @Column(name = "discord_user_id")
    // This value is gained through discord, and is thus not generated.
    private long userId;
    @Column(name = "impera_player_id")
    @Unique
    @Nullable
    private UUID imperaId;
    @Column(name = "super_secret_code")
    @Unique
    private String verificationCode;
    @Column(name = "impera_user_name")
    private String username;

    protected BotUser() {

    }

    public BotUser(long id) {
        this(id, null, PREFER_GUILD_OVER_DMS, null);
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

    public void verifyUser(UUID imperaId, String verificationCode, String username) throws UserAlreadyVerfiedException, IncorrectVerificationCodeException {
        if (!this.verificationCode.equals(verificationCode)) {
            throw new IncorrectVerificationCodeException("Supplied verification code " + verificationCode + " does not match this user's (" + this.userId + ") verification code.");
        }
        if (isLinked()) {
            throw new UserAlreadyVerfiedException("This user (" + this.userId + ") is already linked to an Impera account (" + this.imperaId + ").");
        }
        this.imperaId = imperaId;
        this.username = username;
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

    /**
     * This method generates a new verification code when the verification process is started.
     * Multiple calls to this method will generate new codes, and invalidate the old ones.
     *
     * @return Verification code, call{@link BotUser#verifyUser(UUID, String, String)}with this code and an Impera account to verify the user
     * @throws UserAlreadyVerfiedException User is already linked to an Impera account and they must be unlinked before verification can start again
     * @see BotUser#verifyUser(UUID, String, String)
     */
    public String startVerification() throws UserAlreadyVerfiedException {
        if (isLinked()) {
            throw new UserAlreadyVerfiedException("User " + userId + " is already verified!");
        }
        this.verificationCode = generateVerificationCode();
        return verificationCode;
    }

    public void unlink() {
        this.imperaId = null;
        this.verificationCode = generateVerificationCode();
    }

    public String getUsername() {
        return username;
    }

    public String getMention() {
        return "<@" + this.userId + ">";
    }
}
