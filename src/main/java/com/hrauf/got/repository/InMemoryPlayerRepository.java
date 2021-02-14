package com.hrauf.got.repository;

import com.hrauf.got.model.Player;
import com.hrauf.got.model.PlayerStatus;
import org.springframework.stereotype.Repository;

import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Repository
public class InMemoryPlayerRepository implements PlayerRepository {


    private final AbstractMap<String, Player> store;

    public InMemoryPlayerRepository() {
        store = new ConcurrentHashMap<>();
    }

    @Override
    public void save(Player player) {
        store.put(player.getUsername(), player);
    }

    @Override
    public Optional<Player> findByName(String playerName) {
        return ofNullable(store.get(playerName));
    }

    @Override
    public List<Player> getAvailablePlayers() {
        return store.values().stream().
                filter(p -> p.getStatus() == PlayerStatus.AVAILABLE).collect(Collectors.toList());
    }

    @Override
    public void delete(Player player) {
        store.remove(player.getUsername());
    }

    @Override
    public boolean exists(String playerName) {
        return store.containsKey(playerName);
    }
}
