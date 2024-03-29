package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.UserRepository;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public BotUser verifyUser(String verificationCode, UUID imperaId) {
        try {
            BotUser botUser = userFromOptional(repository.getUserByVerificationCode(verificationCode));
            botUser.setImperaId(imperaId);
            return repository.save(botUser);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Cannot find user with this secret code!");
        }
    }

    public BotUser findUser(long id) {
        try {
            return userFromOptional(repository.findById(id));
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Cannot find user [" + id + "]");
        }
    }

    public BotUser createNewUser(long id) {
        return repository.save(new BotUser(id));
    }

    public BotUser unlinkUser(long id) {
        BotUser botUser = findUser(id);
        botUser.unlink();
        return repository.save(botUser);
    }

    public BotUser findOrCreateUser(long discordId) {
        BotUser botUser;
        try {
            botUser = findUser(discordId);
        } catch (UserNotFoundException e) {
            botUser = createNewUser(discordId);
        }
        return botUser;
    }

    private BotUser userFromOptional(Optional<BotUser> userOptional) {
        return userOptional.orElseThrow(() -> new UserNotFoundException("Could not find user!"));
    }

    public BotUser updateDefaultSetting(long discordId, UserNotificationSetting setting) {
        BotUser user = findOrCreateUser(discordId);
        user.notificationSetting = setting;
        return repository.save(user);
    }

    public Optional<BotUser> findImperaUser(UUID imperaId) {
        return repository.getUserByImperaId(imperaId);
    }
}
