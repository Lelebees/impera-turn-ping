package com.lelebees.imperabot.impera.application;

import com.lelebees.imperabot.impera.data.ImperaRepository;
import com.lelebees.imperabot.impera.domain.game.exception.ImperaGameNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImperaService {
    // TODO: Move logs from ImperaAPIRepository to ImperaService
    private static final Logger logger = LoggerFactory.getLogger(ImperaService.class);
    private final ImperaRepository imperaRepository;

    public ImperaService(ImperaRepository imperaRepository) {
        this.imperaRepository = imperaRepository;
    }

    public List<String> getPlayersThatSurrendered(long gameId, int turnId) {
        return imperaRepository.getActorIdByActionAndTurn(gameId, turnId, "PlayerSurrendered");
    }


    public List<String> getPlayersThatWereDefeated(long gameId, int turnId) {
        return imperaRepository.getActorIdByActionAndTurn(gameId, turnId, "PlayerLost");
    }


    public List<String> getPlayersThatTimedOut(long gameId, int turnId) {
        return imperaRepository.getActorIdByActionAndTurn(gameId, turnId, "PlayerTimeout");
    }

    public void deleteMessageById(String id) {
        imperaRepository.deleteMessage(id);
    }

    public void deleteMessage(ImperaMessageDTO message) {
        deleteMessageById(message.id());
    }

    public List<ImperaMessageDTO> getLinkMessages() {
        return imperaRepository.getMessagesBySubject("Link");
    }

    public ImperaGameViewDTO getGame(long gameId) throws ImperaGameNotFoundException {
        return imperaRepository.findGameByGameId(gameId).orElseThrow(() -> new ImperaGameNotFoundException("Could not find game: " + gameId));
    }
}
