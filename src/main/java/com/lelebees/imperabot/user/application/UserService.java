package com.lelebees.imperabot.user.application;

import com.lelebees.imperabot.user.application.dto.BotUserDTO;
import com.lelebees.imperabot.user.application.exception.UserNotFoundException;
import com.lelebees.imperabot.user.data.UserRepository;
import com.lelebees.imperabot.user.domain.BotUser;
import com.lelebees.imperabot.user.domain.UserNotificationSetting;
import com.lelebees.imperabot.user.domain.exception.IncorrectVerificationCodeException;
import com.lelebees.imperabot.user.domain.exception.UserAlreadyVerfiedException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public BotUserDTO verifyUser(String verificationCode, UUID imperaId, String username) throws UserAlreadyVerfiedException, UserNotFoundException, IncorrectVerificationCodeException {
        try {
            BotUser botUser = userFromOptional(repository.getUserByVerificationCode(verificationCode));
            botUser.verifyUser(imperaId, verificationCode, username);
            return BotUserDTO.from(repository.save(botUser));
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Cannot find user with this verification code (" + verificationCode + ")");
        }
    }

    private BotUser findUser(long id) throws UserNotFoundException {
        try {
            return userFromOptional(repository.findById(id));
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Cannot find user with this id (" + id + ")");
        }
    }

    public BotUserDTO createUser(long id) {
        return BotUserDTO.from(repository.save(new BotUser(id)));
    }

    public BotUserDTO unlinkUser(long id) throws UserNotFoundException {
        BotUser botUser = findUser(id);
        botUser.unlink();
        return BotUserDTO.from(repository.save(botUser));
    }

    public BotUserDTO findOrCreateUser(long discordId) {
        BotUserDTO botUser;
        try {
            botUser = BotUserDTO.from(findUser(discordId));
        } catch (UserNotFoundException e) {
            botUser = createUser(discordId);
        }
        return botUser;
    }

    private BotUser userFromOptional(Optional<BotUser> userOptional) throws UserNotFoundException {
        return userOptional.orElseThrow(() -> new UserNotFoundException("Could not find user!"));
    }

    public BotUserDTO updateDefaultSetting(long discordId, UserNotificationSetting setting) {
        BotUser user;
        try {
            user = findUser(discordId);
        } catch (UserNotFoundException e) {
            user = new BotUser(discordId);
        }
        user.notificationSetting = setting;
        return BotUserDTO.from(repository.save(user));
    }

    public Optional<BotUserDTO> findImperaUser(UUID imperaId) {
        return repository.getUserByImperaId(imperaId).map(BotUserDTO::from);
    }

    public BotUserDTO findImperaUserOrThrow(UUID imperaId) throws UserNotFoundException {
        try {
            return BotUserDTO.from(userFromOptional(repository.getUserByImperaId(imperaId)));
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Cannot find user with this Impera Id (" + imperaId + ")");
        }
    }

    public boolean isImperaUserVerified(UUID imperaId) {
        try {
            return findImperaUserOrThrow(imperaId).isLinked();
        } catch (UserNotFoundException e) {
            return false;
        }
    }

    public String startVerification(long userId) throws UserAlreadyVerfiedException {
        BotUser user;
        try {
            user = findUser(userId);
        } catch (UserNotFoundException e) {
            user = new BotUser(userId);
        }
        String verificationCode = user.startVerification();
        repository.save(user);
        return verificationCode;
    }

    public String getVerificationCode(long userId) throws UserNotFoundException {
        BotUser user = findUser(userId);
        return user.getVerificationCode();
    }
}
