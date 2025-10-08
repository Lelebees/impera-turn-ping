package com.lelebees.imperabot.impera.application;

import com.lelebees.imperabot.impera.data.ImperaRepository;
import com.lelebees.imperabot.impera.domain.ImperaGameActionDTO;
import com.lelebees.imperabot.impera.domain.game.exception.ImperaGameNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.history.HistoryActionName;
import com.lelebees.imperabot.impera.domain.history.ImperaGameHistoryDTO;
import com.lelebees.imperabot.impera.domain.history.exception.TurnNotFoundException;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.lelebees.imperabot.impera.domain.history.HistoryActionName.*;

@Service
public class ImperaService {
    private final Logger logger = LoggerFactory.getLogger(ImperaService.class);
    private final ImperaRepository imperaRepository;

    public ImperaService(ImperaRepository imperaRepository) {
        this.imperaRepository = imperaRepository;
    }

    public HashMap<String, HistoryActionName> getPlayersThatAreNoLongerPlaying(long gameId, int startTurnId, int endTurnId) {
        HashMap<String, HistoryActionName> playersThatNoLongerPlay = new HashMap<>();
        List<HistoryActionName> losingActions = List.of(LOST, SURRENDERED, TIMED_OUT);
        try {
            List<ImperaGameHistoryDTO> previousTurns = imperaRepository.getTurnHistoryInRange(gameId, startTurnId, endTurnId);
            for (ImperaGameHistoryDTO turn : previousTurns) {
                losingActions.forEach(action -> turn.filterAction(action).stream()
                        .map(ImperaGameActionDTO::actorId)
                        .forEach(player -> playersThatNoLongerPlay.put(player, action)));
            }
        } catch (TurnNotFoundException e) {
            logger.warn("Turn not found in game " + gameId + ", throwing IndexOutOfBoundsException!", e);
            throw new IndexOutOfBoundsException("Could not find a turn in game " + gameId);
        }
        return playersThatNoLongerPlay;
    }

    public void deleteMessageById(UUID id) {
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
