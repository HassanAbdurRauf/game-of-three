package com.hrauf.got.repository;

import com.hrauf.got.model.Match;
import org.springframework.stereotype.Repository;

import java.util.AbstractMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMatchRepository implements MatchRepository {

    private AbstractMap<String, Match> matches;

    public InMemoryMatchRepository() {
        matches = new ConcurrentHashMap<>();
    }

    @Override
    public void save(Match match) {
        matches.put(match.getMatchId(), match);
    }

    @Override
    public void remove(String matchID) {
        matches.remove(matchID);
    }

    @Override
    public Optional<Match> findById(String matchID) {
        return Optional.ofNullable(matches.get(matchID));
    }

    public Optional<Match> findByPlayer(String playerId) {
        return matches.values().stream().filter(match -> match.getFirstPlayer().getUsername().equals(playerId)
                || match.getSecondPlayer().getUsername().equals(playerId)).findFirst();
    }

}
