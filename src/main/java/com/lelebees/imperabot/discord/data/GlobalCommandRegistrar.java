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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GlobalCommandRegistrar implements ApplicationRunner {
    final String path = "commands/guild";
    final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
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
        Resource[] guildCommands = matcher.getResources("commands/guild/*.*.json");
        if (guildCommands.length == 0) {
            LOGGER.info("No guild commands found.");
            return;
        }
        Map<Long, List<ApplicationCommandRequest>> commandsByGuild = new HashMap<>();
        for (Resource guildCommand : guildCommands) {
            Long guildId = Long.parseLong(guildCommand.getFilename().split("\\.")[0]);
            if (!commandsByGuild.containsKey(guildId)) {
                commandsByGuild.put(guildId, new ArrayList<>());
            }
            ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                    .readValue(guildCommand.getInputStream(), ApplicationCommandRequest.class);

            commandsByGuild.get(guildId).add(request);
        }
        for (Map.Entry<Long, List<ApplicationCommandRequest>> guildCommandSet : commandsByGuild.entrySet()) {
            applicationService.bulkOverwriteGuildApplicationCommand(applicationId, guildCommandSet.getKey(), guildCommandSet.getValue())
                    .doOnNext(ignore -> LOGGER.info("Successfully registered {} commands for {}", guildCommandSet.getValue().size(), guildCommandSet.getKey()))
                    .doOnError(e -> LOGGER.error("Failed to register commands for {}", guildCommandSet.getKey(), e))
                    .subscribe();
        }
    }
}
