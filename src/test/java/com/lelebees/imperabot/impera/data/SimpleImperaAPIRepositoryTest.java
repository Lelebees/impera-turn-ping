package com.lelebees.imperabot.impera.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SimpleImperaAPIRepositoryTest {
    String dummy_dto = """
            {
              "id": 0,
              "type": "Fun",
              "name": "string",
              "hasPassword": true,
              "mapTemplate": "string",
              "teams": [
                {
                  "id": "string",
                  "playOrder": 0,
                  "players": [
                    {
                      "id": "string",
                      "userId": "string",
                      "name": "string",
                      "state": "None",
                      "outcome": "None",
                      "teamId": "string",
                      "playOrder": 0,
                      "timeouts": 0,
                      "cards": [
                        "A"
                      ],
                      "placedInitialUnits": true,
                      "numberOfUnits": 0,
                      "numberOfCountries": 0
                    }
                  ]
                }
              ],
              "state": "None",
              "playState": "None",
              "currentPlayer": {
                "id": "string",
                "userId": "string",
                "name": "string",
                "state": "None",
                "outcome": "None",
                "teamId": "string",
                "playOrder": 0,
                "timeouts": 0
              },
              "map": {
                "countries": [
                  {
                    "identifier": "string",
                    "playerId": "string",
                    "teamId": "string",
                    "units": 0,
                    "flags": "None"
                  }
                ]
              },
              "options": {
                "numberOfPlayersPerTeam": 8,
                "numberOfTeams": 16,
                "minUnitsPerCountry": 5,
                "newUnitsPerTurn": 10,
                "attacksPerTurn": 100,
                "movesPerTurn": 100,
                "initialCountryUnits": 5,
                "mapDistribution": "Default",
                "timeoutInSeconds": 432000,
                "maximumTimeoutsPerPlayer": 0,
                "maximumNumberOfCards": 10,
                "victoryConditions": [
                  "Survival"
                ],
                "visibilityModifier": [
                  "None"
                ]
              },
              "lastModifiedAt": "2025-09-13T13:39:48.993Z",
              "timeoutSecondsLeft": 0,
              "turnCounter": 0,
              "unitsToPlace": 0,
              "attacksInCurrentTurn": 0,
              "movesInCurrentTurn": 0
            }
            """;

    @Test
    @DisplayName("Parsing found game returns found game")
    void findGameByGameId() {
        SimpleImperaAPIRepository repository = new SimpleImperaAPIRepository("https://localhost:8080", "neither do i", "and neither do i", new DummyHttpClient(dummy_dto));
        assertNotEquals(null, repository.findGameByGameId(0).get());
    }

    public static class DummyHttpClient extends HttpClient {
        private String dummyResponse;

        public DummyHttpClient(String dummyResponse) {
            this.dummyResponse = dummyResponse;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return null;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return null;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
            return (HttpResponse<T>) new DummyHttpResponse(dummyResponse, 200);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            return null;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            return null;
        }
    }

    public static class DummyHttpResponse implements HttpResponse<String> {
        private String dummyResponse;
        private int dummyStatus;

        public DummyHttpResponse(String dummyResponse, int dummyStatus) {
            this.dummyResponse = dummyResponse;
            this.dummyStatus = dummyStatus;
        }

        @Override
        public int statusCode() {
            return dummyStatus;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return null;
        }

        @Override
        public String body() {
            return dummyResponse;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return null;
        }

        @Override
        public HttpClient.Version version() {
            return null;
        }
    }
}