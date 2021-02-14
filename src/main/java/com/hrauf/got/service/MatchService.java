package com.hrauf.got.service;

import com.hrauf.got.exception.OpponentMissingException;
import com.hrauf.got.model.Match;
import com.hrauf.got.model.Player;

import java.util.Optional;

public interface MatchService {
    Match createMatch(Player player1, Player player2);
    Match find(String matchId);
    Optional<Match> findByPlayer(String playerId);
    void endMatch(String matchId);
    Player getOpponent(Match match, String playerId) throws OpponentMissingException;
}
