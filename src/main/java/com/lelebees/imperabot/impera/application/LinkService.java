package com.lelebees.imperabot.impera.application;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.lelebees.imperabot.ImperaBotApplication.env;

@Service
public class LinkService {
    private final String imperaURL = env.get("IMPERA_API_URL");
    private final HttpEntity<String> entity;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ImperaService imperaService;
    private final UserService userService;

    public LinkService(ImperaService imperaService, UserService userService) {
        this.imperaService = imperaService;
        this.userService = userService;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.imperaService.getBearerToken().access_token);
        this.entity = new HttpEntity<>(headers);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        final ScheduledFuture<?> linkHandle = executorService.scheduleAtFixedRate(checkVerifyRequests(), 0, 1, TimeUnit.MINUTES);
    }

    public Runnable checkVerifyRequests() {
        return () -> {
            List<ImperaMessageDTO> linkMessages = imperaService.getLinkMessages();
            for (ImperaMessageDTO linkMessage : linkMessages) {
                try {
                    userService.verifyUser(linkMessage.text, UUID.fromString(linkMessage.from.id));
                } catch (UserNotFoundException e) {
                    System.out.println("User matching code " + linkMessage.text + " Not found, skipping...");
                }
            }
            System.out.println("Test");
        };
    }
}
