package com.hrauf.got.repository;

import com.hrauf.got.model.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {

    void save(Player player);

    Optional<Player> findByName(String playerName);

    boolean exists(String playerName);

    List<Player> getAvailablePlayers();

    void delete(Player player);
}
