package com.lelebees.imperabot.impera.data;

import com.lelebees.imperabot.impera.domain.ImperaLoginDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.history.ImperaGameHistoryDTO;
import com.lelebees.imperabot.impera.domain.history.exception.TurnNotFoundException;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import discord4j.common.JacksonResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SimpleImperaAPIRepository implements ImperaRepository {
    private final Logger logger = LoggerFactory.getLogger(SimpleImperaAPIRepository.class);
    private final String imperaURL;
    private final String imperaUsername;
    private final String imperaPassword;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JacksonResources imperaMapper = JacksonResources.create();
    private String bearerToken;

    public SimpleImperaAPIRepository(@Value("${impera.api.url}") String imperaURL, @Value("${impera.username}") String imperaUsername, @Value("${impera.password}") String imperaPassword) {
        this.imperaURL = imperaURL;
        this.imperaUsername = imperaUsername;
        this.imperaPassword = imperaPassword;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::updateBearerToken, 0, 1, TimeUnit.HOURS);

    }


    @Override
    public Optional<ImperaGameViewDTO> findGameByGameId(long gameId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imperaURL + "/games/" + gameId))
                .header("Authorization", "Bearer %s".formatted(bearerToken))
                .GET()
                .build();
        try {
            return Optional.of(imperaMapper.getObjectMapper().readValue(client.send(request, HttpResponse.BodyHandlers.ofString()).body(), ImperaGameViewDTO.class));
        } catch (IOException e) {
            logger.error("IOException attempting to get game %d from impera API".formatted(gameId), e);
        } catch (InterruptedException e) {
            logger.error("InterruptedException attempting to get game %d from impera API".formatted(gameId), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ImperaMessageDTO> getMessages() {
        return List.of();
    }

    @Override
    public List<ImperaMessageDTO> getMessagesBySubject(String subject) {
        return List.of();
    }

    @Override
    public void deleteMessage(String id) {

    }

    @Override
    public List<ImperaGameHistoryDTO> getTurnHistoryInRange(long gameId, int startTurnId, int endTurnId) throws TurnNotFoundException {
        return List.of();
    }

    private void updateBearerToken() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imperaURL + "/Account/token"))
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=password&username=" + imperaUsername + "&password=" + imperaPassword + "&scope=openid%20offline_access%20roles&"))
                    .build();
            ImperaLoginDTO dto = imperaMapper.getObjectMapper().readValue(client.send(request, HttpResponse.BodyHandlers.ofString()).body(), ImperaLoginDTO.class);
            bearerToken = dto.access_token();
        } catch (RuntimeException | IOException | InterruptedException e) {
            logger.error("An Exception occurred while refreshing bearer token. To avoid silently failing, the error was caught and this thread will continue.", e);
        }
    }
}
