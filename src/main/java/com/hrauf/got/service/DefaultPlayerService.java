package com.hrauf.got.service;

import com.hrauf.got.exception.PlayerNotFoundException;
import com.hrauf.got.model.PlayerStatus;
import com.hrauf.got.repository.PlayerRepository;
import com.hrauf.got.model.Player;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Component
public class DefaultPlayerService implements PlayerService{

    public static final String PLAYER_NOT_FOUND = "Player Not Found: %s";

    private final PlayerRepository playerRepository;

    public Player findPlayer(String playerName) throws PlayerNotFoundException {
        return playerRepository.findByName(playerName).orElseThrow(() -> new PlayerNotFoundException(String.format(PLAYER_NOT_FOUND, playerName)));
    }

    @Override
    public boolean exists(String playerName) {
        return playerRepository.exists(playerName);
    }

    public void save(Player player) {
        playerRepository.save(player);
    }

    public Optional<Player> findNewOpponent(Player player) {
        return playerRepository.getAvailablePlayers().stream().filter(p -> !p.getUsername().equals(player.getUsername()))
                .findFirst();
    }

    public void updatePlayerStatusPaired(Player player) {
        player.setStatus(PlayerStatus.PAIRED);
        playerRepository.save(player);
    }

    public void updatePlayerStatusAvailable(Player player){
        player.setStatus(PlayerStatus.AVAILABLE);
        playerRepository.save(player);
    }

    public void removePlayer(String playerName) {
        playerRepository.findByName(playerName)
                .ifPresent(playerRepository::delete);
    }
}
