package com.hrauf.got.service;

import com.hrauf.got.model.Player;

import java.util.Optional;

public interface PlayerService {
    Player findPlayer(String playerName);
    boolean exists(String playerName);
    void save(Player player);
    Optional<Player> findNewOpponent(Player player);
    void updatePlayerStatusPaired(Player player);
    void updatePlayerStatusAvailable(Player player);
    void removePlayer(String playerId);
}
