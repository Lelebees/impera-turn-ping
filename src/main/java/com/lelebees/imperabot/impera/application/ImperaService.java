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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.lelebees.imperabot.impera.domain.history.HistoryActionName.*;

@Service
public class ImperaService {
    // TODO: Move logs from ImperaAPIRepository to ImperaService
    private static final Logger logger = LoggerFactory.getLogger(ImperaService.class);
    private final ImperaRepository imperaRepository;

    public ImperaService(ImperaRepository imperaRepository) {
        this.imperaRepository = imperaRepository;
    }

    public HashMap<HistoryActionName, List<String>> getPlayersThatAreNoLongerPlaying(long gameId, int startTurnId, int endTurnId) {
        HashMap<HistoryActionName, List<String>> playersLists = new HashMap<>();
        playersLists.put(SURRENDERED, new ArrayList<>());
        playersLists.put(LOST, new ArrayList<>());
        playersLists.put(TIMED_OUT, new ArrayList<>());
        try {
            List<ImperaGameHistoryDTO> previousTurns = imperaRepository.getTurnHistoryInRange(gameId, startTurnId, endTurnId);
            previousTurns.forEach(turn -> playersLists.keySet()
                    .forEach(key -> playersLists.get(key).addAll(turn.filterAction(key.toString()).stream()
                            .map(ImperaGameActionDTO::actorId).toList())));
            return playersLists;
        } catch (TurnNotFoundException e) {
            logger.warn("Turn not found, throwing IndexOutOfBoundsException!", e);
            throw new IndexOutOfBoundsException("Could not find a turn in game " + gameId);
        }
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
