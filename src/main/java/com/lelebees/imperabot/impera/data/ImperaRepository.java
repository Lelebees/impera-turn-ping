package com.lelebees.imperabot.impera.data;

import com.lelebees.imperabot.impera.application.exception.TurnNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.history.ImperaGameHistoryDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImperaRepository {

    Optional<ImperaGameViewDTO> findGameByGameId(long gameId);

    List<ImperaMessageDTO> getMessages();

    List<ImperaMessageDTO> getMessagesBySubject(String subject);

    void deleteMessage(UUID id);

    List<ImperaGameHistoryDTO> getTurnHistoryInRange(long gameId, int startTurnId, int endTurnId) throws TurnNotFoundException;
}
