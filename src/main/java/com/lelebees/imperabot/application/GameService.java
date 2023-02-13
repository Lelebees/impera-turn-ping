package com.lelebees.imperabot.application;

import com.lelebees.imperabot.data.GameRepository;
import com.lelebees.imperabot.domain.game.Game;
import com.lelebees.imperabot.domain.game.GameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    //TODO: Implement Impera calls
    public void checkGamesForPing()
    {
        List<Game> gameList = findAllGames();
        for (Game game:gameList) {
            // Check if it's someone's turn!
            if (game.last_turn < 1)
            {
                //Do something!
            }
            if (game.half_time_notice == false && true)
            {
                //Do something when it's half-time!
            }
        }
    }

    public Game findGameByID(int ID){
        Optional<Game> gameOptional = gameRepository.findById(ID);
        if (gameOptional.isPresent())
        {
            return gameOptional.get();
        }
        throw new GameNotFoundException("Could not find game: "+ID);
    }

    public List<Game> findAllGames()
    {
        return gameRepository.findAll();
    }
}
