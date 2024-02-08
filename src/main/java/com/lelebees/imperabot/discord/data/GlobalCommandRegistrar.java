package com.lelebees.imperabot.discord.data;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class GlobalCommandRegistrar implements ApplicationRunner {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final RestClient client;

    //Use the rest client provided by our Bean
    public GlobalCommandRegistrar(RestClient client) {
        this.client = client;
    }

    //This method will run only once on each start up and is automatically called with Spring so blocking is okay.
    @Override
    public void run(ApplicationArguments args) throws IOException {
        //Create an ObjectMapper that supported Discord4J classes
        final JacksonResources d4jMapper = JacksonResources.create();

        // Convenience variables for the sake of easier to read code below.
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
        final ApplicationService applicationService = client.getApplicationService();
        final long applicationId = client.getApplicationId().block();

        //Get our commands json from resources as command data
        List<ApplicationCommandRequest> commands = new ArrayList<>();
        for (Resource resource : matcher.getResources("commands/global/*.json")) {
            ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                    .readValue(resource.getInputStream(), ApplicationCommandRequest.class);

            commands.add(request);
        }

        /* Bulk overwrite commands. This is now idempotent, so it is safe to use this even when only 1 command
        is changed/added/removed
        */
        applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, commands)
                .doOnNext(ignore -> LOGGER.debug("Successfully registered Global Commands"))
                .doOnError(e -> LOGGER.error("Failed to register global commands", e))
                .subscribe();

        //Do the same for each GuildCommand
        File file = new File("src/main/resources/commands/guild/");
        String[] strArr = file.list((current, name) -> new File(current, name).isDirectory());
        if (strArr == null) {
            LOGGER.debug("No guilds found!");
            return;
        }
        List<String> guilds = Arrays.stream(strArr).toList();
        for (String guildId : guilds) {
            List<ApplicationCommandRequest> guildCommands = new ArrayList<>();
            for (Resource resource : matcher.getResources("commands/guild/" + guildId + "/*.json")) {
                ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                        .readValue(resource.getInputStream(), ApplicationCommandRequest.class);

                guildCommands.add(request);
            }
            applicationService.bulkOverwriteGuildApplicationCommand(applicationId, Long.parseLong(guildId), guildCommands)
                    .doOnNext(ignore -> LOGGER.debug("Successfully registered commands for " + guildId))
                    .doOnError(e -> LOGGER.error("Failed to register commands for " + guildId, e))
                    .subscribe();
        }
    }
}
