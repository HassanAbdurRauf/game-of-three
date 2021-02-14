package com.hrauf.got.service;

import com.hrauf.got.exception.InvalidMoveException;
import com.hrauf.got.exception.MatchNotFoundException;
import com.hrauf.got.exception.OpponentMissingException;
import com.hrauf.got.exception.PlayerNotFoundException;
import com.hrauf.got.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.hrauf.got.model.EventFactory.*;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultGameOfThreeService implements GameOfThreeService{

    private static final int DIVISOR = 3;
    public static final int FINAL_WIN_VALUE = 1;
    public static final String NOT_DIVISIBLE_BY_ZERO = "%d is not divisible by %d";
    public static final String OPPONENT_NOT_FOUND = "Opponent not found";

    private final PlayerService playerService;
    private final MatchService matchService;
    private final NotificationService notificationService;


    public Event startGame(String playerName) throws PlayerNotFoundException {
        Player player = playerService.findPlayer(playerName);
        Event message = EventFactory.buildWaitingMessage();

        Optional<Player> possibleOpponent = playerService.findNewOpponent(player);
        if (possibleOpponent.isPresent()) {
            playerService.updatePlayerStatusPaired(player);
            playerService.updatePlayerStatusPaired(possibleOpponent.get());
            Match match = matchService.createMatch(player, possibleOpponent.get());

            notifyPlayer(possibleOpponent.get(), buildStartMessage(player, match));

            message = buildStartMessage(possibleOpponent.get(), match);
        }

        return message;
    }

    public void processMove(String playerName, GameInstruction gameInstruction)
            throws PlayerNotFoundException, MatchNotFoundException, OpponentMissingException {

        Player player =  playerService.findPlayer(playerName);
        Match match = matchService.find(gameInstruction.getMatchId());
        Optional.ofNullable(matchService.getOpponent(match, player.getUsername())).ifPresentOrElse(opponent -> {
            if (isRandomNumberInstruction(gameInstruction.getRandomNumber())) {
                notificationService.notifyPlayer(opponent.getUsername(), buildPlayMessage(gameInstruction.getRandomNumber()));
            } else {
                int newValue = gameInstruction.getValue() / DIVISOR ;
                logPlayerMove(playerName, gameInstruction, newValue);
                checkDivisibleByDivisor(gameInstruction.getValue());
                if (isWinningState(newValue)) {
                    notificationService.notifyPlayer(player.getUsername(), buildGameOverMessage(true));
                    notificationService.notifyPlayer(opponent.getUsername(), buildGameOverMessage(false));
                } else {
                    notificationService.notifyPlayer(opponent.getUsername(), buildPlayMessage(newValue));
                }
            }

        }, this::throwOpponentMissingException);


    }

    private boolean isWinningState(int newValue) {
        return newValue == FINAL_WIN_VALUE;
    }

    private boolean isRandomNumberInstruction(int randomNumber) {
        return randomNumber != 0;
    }

    private void notifyPlayer(Player player, Event message) {
        notificationService.notifyPlayer(player.getUsername(), message);
    }

    private void checkDivisibleByDivisor(int number) {
        if (number % DIVISOR != 0) {
            throw new InvalidMoveException(String.format(NOT_DIVISIBLE_BY_ZERO, number, DIVISOR));
        }
    }

    private void logPlayerMove(String playerName, GameInstruction gameInstruction, int updatedGameValue) {
        log.debug("{} got value: {} and result after division by {}: {}",
                playerName,
                gameInstruction.getValue(),
                DIVISOR,
                updatedGameValue);
    }

    public void throwOpponentMissingException() {
        throw new OpponentMissingException(OPPONENT_NOT_FOUND);
    }
}
