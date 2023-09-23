package com.lelebees.imperabot;

import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.ImperaLoginDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ImperaServiceTest {
    @Autowired
    ImperaService imperaService;

    @Test
    @DisplayName("getBearerToken with refresh token will refresh bearer token")
    public void getBearerTokenRefresh() {
        ImperaLoginDTO loginDTO1 = imperaService.getBearerToken(null);
        ImperaLoginDTO loginDTO2 = imperaService.getBearerToken(loginDTO1.refresh_token);
        Assertions.assertNotEquals(loginDTO1.access_token, loginDTO2.access_token);
    }

    @Test
    @DisplayName("access to a game does not require being in it")
    @Disabled
    public void testAccessToEveryone() {
        ImperaGameViewDTO dto = imperaService.getGame(395948);
        Assertions.assertEquals(395948, dto.id);
    }
}
