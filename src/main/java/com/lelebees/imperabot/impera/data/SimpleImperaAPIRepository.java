package com.lelebees.imperabot.impera.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lelebees.imperabot.impera.data.exception.CouldNotAuthorizeException;
import com.lelebees.imperabot.impera.data.exception.UnauthorizedException;
import com.lelebees.imperabot.impera.domain.ExceptionModel;
import com.lelebees.imperabot.impera.domain.ImperaLoginDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.history.ImperaGameHistoryDTO;
import com.lelebees.imperabot.impera.domain.history.exception.TurnNotFoundException;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import discord4j.common.JacksonResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class SimpleImperaAPIRepository implements ImperaRepository {
    private final Logger logger = LoggerFactory.getLogger(SimpleImperaAPIRepository.class);
    private final String imperaURL;
    private final String imperaUsername;
    private final String imperaPassword;
    private final HttpClient client;
    private final JacksonResources imperaMapper = JacksonResources.create();
    private String bearerToken;

    // Full Constructor
    public SimpleImperaAPIRepository(String imperaURL, String imperaUsername, String imperaPassword, HttpClient client) {
        this.imperaURL = imperaURL;
        this.imperaUsername = imperaUsername;
        this.imperaPassword = imperaPassword;
        this.client = client;
//        updateBearerToken();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::updateBearerToken, 0, 1, TimeUnit.HOURS);
    }

    //Spring Constructor (this is the one that will be called)
    @Autowired
    public SimpleImperaAPIRepository(@Value("${impera.api.url}") String imperaURL, @Value("${impera.username}") String imperaUsername, @Value("${impera.password}") String imperaPassword) {
        this(imperaURL, imperaUsername, imperaPassword, HttpClient.newHttpClient());
    }


    @Override
    public Optional<ImperaGameViewDTO> findGameByGameId(long gameId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imperaURL + "/games/" + gameId))
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer %s".formatted(bearerToken))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                handleNon200Status(response);
                return Optional.empty();
            }
            return Optional.of(imperaMapper.getObjectMapper().readValue(response.body(), ImperaGameViewDTO.class));
        } catch (JsonProcessingException e) {
            logger.error("Jackson could not parse returned object attempting to get game %d from Impera API".formatted(gameId), e);
        } catch (IOException e) {
            logger.error("IOException attempting to get game %d from impera API".formatted(gameId), e);
        } catch (InterruptedException e) {
            logger.error("InterruptedException attempting to get game %d from impera API".formatted(gameId), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ImperaMessageDTO> getMessages() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imperaURL + "/messages/folder/Inbox"))
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer %s".formatted(bearerToken))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                handleNon200Status(response);
                return List.of();
            }
            return List.of(imperaMapper.getObjectMapper().readValue(response.body(), ImperaMessageDTO[].class));
        } catch (JsonProcessingException e) {
            logger.error("Jackson could not parse returned objects attempting to get messages from Impera API", e);
        } catch (IOException e) {
            logger.error("IOException attempting to get messages from impera API", e);
        } catch (InterruptedException e) {
            logger.error("InterruptedException attempting to get messages from impera API", e);
        }
        return List.of();
    }

    @Override
    public List<ImperaMessageDTO> getMessagesBySubject(String subject) {
        return getMessages().stream()
                .filter(message -> message.subject().trim().equalsIgnoreCase(subject))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMessage(String id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imperaURL + "/messages/" + id))
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer %s".formatted(bearerToken))
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                handleNon200Status(response);
            }
        } catch (JsonProcessingException e) {
            logger.error("Jackson could not parse returned objects attempting to get messages from Impera API", e);
        } catch (IOException e) {
            logger.error("IOException attempting to get messages from impera API", e);
        } catch (InterruptedException e) {
            logger.error("InterruptedException attempting to get messages from impera API", e);
        }
    }

    @Override
    public List<ImperaGameHistoryDTO> getTurnHistoryInRange(long gameId, int startTurnId, int endTurnId) throws TurnNotFoundException {
        List<ImperaGameHistoryDTO> turnStates = new ArrayList<>();
        for (int turnId = startTurnId; turnId < endTurnId; turnId++) {
            Optional<ImperaGameHistoryDTO> turnState = getTurnState(gameId, turnId);
            if (turnState.isEmpty()) {
                throw new TurnNotFoundException("Could not find turn " + turnId + " in game " + gameId + ".");
            }
            turnStates.add(turnState.get());
        }
        return turnStates;
    }

    private Optional<ImperaGameHistoryDTO> getTurnState(long gameId, int turnId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imperaURL + "/games/" + gameId + "/history/" + turnId))
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer %s".formatted(bearerToken))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                handleNon200Status(response);
                return Optional.empty();
            }
            return Optional.of(imperaMapper.getObjectMapper().readValue(response.body(), ImperaGameHistoryDTO.class));
        } catch (JsonProcessingException e) {
            logger.error("Jackson could not parse returned objects attempting to get messages from Impera API", e);
        } catch (IOException e) {
            logger.error("IOException attempting to get messages from impera API", e);
        } catch (InterruptedException e) {
            logger.error("InterruptedException attempting to get messages from impera API", e);
        }
        return Optional.empty();
    }

    private void updateBearerToken() throws CouldNotAuthorizeException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imperaURL + "/Account/token"))
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=password&username=" + imperaUsername + "&password=" + imperaPassword + "&scope=openid%20offline_access%20roles&"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new CouldNotAuthorizeException("Couldn't authorize with Impera, %d: %s".formatted(response.statusCode(), response.body()));
            }
            ImperaLoginDTO dto = imperaMapper.getObjectMapper().readValue(response.body(), ImperaLoginDTO.class);
            bearerToken = dto.access_token().strip().replaceAll("[\n\r]+", "");
            logger.info("Updated Impera bearer token");
        } catch (IOException | InterruptedException e) {
            logger.error("An Exception occurred while refreshing bearer token. To avoid silently failing, the error was caught and this thread will continue.", e);
        } catch (RuntimeException e) {
            logger.error("A Runtime exception occurred! Rethrowing...");
            throw new RuntimeException(e);
        }
    }

    private void handleNon200Status(HttpResponse<String> response) throws JsonProcessingException, UnauthorizedException {
        if (response.statusCode() == 200) {
            return;
        }
        switch (response.statusCode()) {
            case 401:
                try {
                    updateBearerToken();
                } catch (CouldNotAuthorizeException e) {
                    throw new RuntimeException("Could not Authorize with impera", e);
                }
                throw new UnauthorizedException("Request response was 401: Unauthorized, updated bearer token, please retry.");
            case 400:
                ExceptionModel imperaException = imperaMapper.getObjectMapper().readValue(response.body(), ExceptionModel.class);
                logger.error("Impera API returned bad request: %s, %s".formatted(imperaException.error(), imperaException.error_Description()));
        }
    }
}
