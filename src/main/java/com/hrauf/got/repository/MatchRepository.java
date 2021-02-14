package com.hrauf.got.repository;

import com.hrauf.got.model.Match;

import java.util.Optional;

public interface MatchRepository {
    public void save(Match match);
    public void remove(String matchId);
    public Optional<Match> findById(String matchId);
    public Optional<Match> findByPlayer(String playerId);
}
