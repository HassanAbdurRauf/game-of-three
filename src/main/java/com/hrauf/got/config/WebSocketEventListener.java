package com.hrauf.got.config;

import com.hrauf.got.model.EventFactory;
import com.hrauf.got.model.PlayerStatus;
import com.hrauf.got.service.DefaultPlayerService;
import com.hrauf.got.service.MatchService;
import com.hrauf.got.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hrauf.got.model.Player;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static com.hrauf.got.config.WebSocketConfig.USERNAME_HEADER;

@Slf4j
@Component
@AllArgsConstructor
public class WebSocketEventListener {

    private final DefaultPlayerService playerService;
    private final MatchService matchService;
    private final NotificationService notificationService;

    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        log.debug("player connected: {}", event.getUser().getName());
        playerService.save(new Player(event.getUser().getName()));
    }


    @EventListener
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        ofNullable(accessor.getSessionAttributes().get(USERNAME_HEADER))
                .map(String.class::cast)
                .ifPresent(username -> {
                    log.debug("Player disconnected: {}", username);
                    Optional.ofNullable(playerService.findPlayer(username))
                            .map(Player::getStatus)
                            .filter(s -> s.equals(PlayerStatus.PAIRED))
                            .flatMap(playerStatus -> matchService.findByPlayer(username)).ifPresent(m -> {
                        Player opponent = matchService.getOpponent(m, username);
                        notifyOpponentDisconnect(opponent);
                        playerService.updatePlayerStatusAvailable(opponent);
                        matchService.endMatch(m.getMatchId());
                    });
                    playerService.removePlayer(username);
                });

    }

    private void notifyOpponentDisconnect(Player player) {
        notificationService.notifyPlayer(player.getUsername(), EventFactory.buildDisconnectMessage("Your opponent has left the game."));
    }
}
