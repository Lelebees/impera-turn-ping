package com.lelebees.imperabot.user.application;

import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageCommunicatorDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import com.lelebees.imperabot.user.application.dto.BotUserDTO;
import com.lelebees.imperabot.user.application.exception.UserNotFoundException;
import com.lelebees.imperabot.user.data.UserRepository;
import com.lelebees.imperabot.user.domain.BotUser;
import com.lelebees.imperabot.user.domain.UserNotificationSetting;
import com.lelebees.imperabot.user.domain.exception.IncorrectVerificationCodeException;
import com.lelebees.imperabot.user.domain.exception.UserAlreadyVerfiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final ImperaService imperaService;
    private final DiscordService discordService;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository repository, ImperaService imperaService, DiscordService discordService) {
        this.repository = repository;
        this.imperaService = imperaService;
        this.discordService = discordService;
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

    public void checkVerificationMessages() {
        List<ImperaMessageDTO> linkMessages = imperaService.getLinkMessages();
        logger.info("Found {} link requests.", linkMessages.size());
        int skippedRequests = 0;
        for (ImperaMessageDTO linkMessage : linkMessages) {
            ImperaMessageCommunicatorDTO sender = linkMessage.from();
            Optional<BotUserDTO> potentialImperaUser = findImperaUser(sender.id());
            if (potentialImperaUser.isPresent()) {
                BotUserDTO imperaUser = potentialImperaUser.get();
                skippedRequests++;
                logger.warn("User with Impera account {} ({}) already exists. (is {} ({})) Skipping and destroying message...", sender.name(), sender.id(), imperaUser.username(), imperaUser.discordId());
                imperaService.deleteMessage(linkMessage);
                continue;
            }
            BotUser user;
            try {
                user = userFromOptional(repository.getUserByVerificationCode(linkMessage.getTrimmedText()));
            } catch (UserNotFoundException e) {
                logger.warn("User matching code {} Not found, skipping...", linkMessage.text());
                skippedRequests++;
                continue;
            }
            try {
                user.verifyUser(sender.id(), linkMessage.getTrimmedText(), sender.name());
                logger.info("User {} ({}) aka (snowflake) {} has been verified!", sender.name(), sender.id().toString(), user.getUserId());
                discordService.sendVerificationDM(user.getUserId());
            } catch (UserAlreadyVerfiedException e) {
                logger.warn("User {} ({}) aka (snowflake) {} already verified!", sender.name(), sender.id().toString(), user.getUserId());
            } catch (IncorrectVerificationCodeException e) {
                logger.warn("User {} ({}) could not be verified as (snowflake) {} because the supplied verification code was incorrect.", sender.name(), sender.id(), user.getUserId());
            }
            imperaService.deleteMessage(linkMessage);
        }
        logger.info("Skipped {} requests.", skippedRequests);
    }
}
