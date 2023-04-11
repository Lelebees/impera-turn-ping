package com.lelebees.imperabot.presentation;

import com.lelebees.imperabot.application.impera.ImperaService;
import com.lelebees.imperabot.domain.impera.ImperaLoginDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// This totally isn't a hack so I can test shit. SHHHHH
@Controller
@RequestMapping("/test")
public class TestController {
    private final ImperaService imperaService;

    public TestController(ImperaService imperaService) {
        this.imperaService = imperaService;
    }

    @GetMapping("/bearer")
    public ImperaLoginDTO testBearerAuth()
    {
        return imperaService.getBearerToken();
    }
}
