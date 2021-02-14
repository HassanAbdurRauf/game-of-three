package com.hrauf.got.service;

import com.hrauf.got.exception.MatchNotFoundException;
import com.hrauf.got.model.Match;
import com.hrauf.got.model.Player;
import com.hrauf.got.repository.MatchRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Component
public class DefaultMatchService implements MatchService {

    public static final String MATCH_NOT_FOUND = "Match not found: %s";

    MatchRepository matchRepository;

    @Override
    public Match createMatch(Player player1, Player player2) {
        Match match = Match.builder()
                .matchId(UUID.randomUUID().toString())
                .firstPlayer(player1)
                .secondPlayer(player2)
                .build();
        matchRepository.save(match);
        return match;
    }

    @Override
    public Match find(String matchId) throws MatchNotFoundException {
        return matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(String.format(MATCH_NOT_FOUND, matchId)));
    }

    @Override
    public Optional<Match> findByPlayer(String playerId) {
        return matchRepository.findByPlayer(playerId);
    }

    @Override
    public void endMatch(String matchId) {
        matchRepository.remove(matchId);
    }

    @Override
    public Player getOpponent(Match match, String playerId) {
        return match.getFirstPlayer().getUsername().equals(playerId) ? match.getSecondPlayer() : match.getFirstPlayer();
    }
}
