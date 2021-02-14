package com.hrauf.got.config;

import com.hrauf.got.model.EventFactory;
import com.hrauf.got.model.Match;
import com.hrauf.got.model.PlayerStatus;
import com.hrauf.got.service.DefaultMatchService;
import com.hrauf.got.service.DefaultPlayerService;
import com.hrauf.got.service.NotificationService;
import com.hrauf.got.model.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Optional;

import static com.hrauf.got.common.TestCommon.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketEventListenerTest {

    @Mock
    private DefaultPlayerService playerService;

    @Mock
    private DefaultMatchService matchService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SessionConnectedEvent sessionConnectedEvent;

    @InjectMocks
    private WebSocketEventListener listener;


    @Test
    public void handleWebSocketConnected_PlayerJoined_SavedPlayer() {
        // given
        Mockito.when(sessionConnectedEvent.getUser()).thenReturn(()->PLAYER_1_NAME);

        // when
        listener.handleWebSocketConnected(sessionConnectedEvent);

        // then
        verify(playerService).save(TEST_PLAYER_1);
    }

    @Test
    public void handleWebSocketDisconnected_NoMatchInProgress_RemovePlayer() {
        // given
        var sessionMap = new HashMap<String, Object>();
        sessionMap.put(USERNAME, PLAYER_1_NAME);

        var message = MessageBuilder.withPayload(new byte[0])
                .setHeader(StompHeaderAccessor.SESSION_ATTRIBUTES, sessionMap)
                .build();

        var sessionDisconnectEvent = new SessionDisconnectEvent(new Object(), message, SESSION_ID, CloseStatus.NORMAL);

        // when
        listener.handleWebSocketDisconnected(sessionDisconnectEvent);

        // then
        verify(playerService).removePlayer(PLAYER_1_NAME);
    }

    @Test
    public void handleWebSocketDisconnected_MatchInProgress_RemovePlayerAndEndMatch() {
        // given
        var sessionMap = new HashMap<String, Object>();
        sessionMap.put(USERNAME, PLAYER_1_NAME);

        Player pairedPlayer1 = new Player(PLAYER_1_NAME);
        pairedPlayer1.setStatus(PlayerStatus.PAIRED);

        Player pairedPlayer2 = new Player(PLAYER_2_NAME);
        pairedPlayer2.setStatus(PlayerStatus.PAIRED);

        Match testMatch = Match.builder()
                .secondPlayer(pairedPlayer2)
                .firstPlayer(pairedPlayer1)
                .matchId(TEST_MATCH_ID)
                .build();

        Mockito.when(playerService.findPlayer(PLAYER_1_NAME)).thenReturn(pairedPlayer1);
        Mockito.when(matchService.findByPlayer(PLAYER_1_NAME)).thenReturn(Optional.of(testMatch));
        Mockito.doNothing().when(notificationService).notifyPlayer(PLAYER_2_NAME,
                EventFactory.buildDisconnectMessage("Your opponent has left the game."));
        Mockito.when(matchService.getOpponent(testMatch, PLAYER_1_NAME)).thenReturn(pairedPlayer2);

        var message = MessageBuilder.withPayload(new byte[0])
                .setHeader(StompHeaderAccessor.SESSION_ATTRIBUTES, sessionMap)
                .build();

        var sessionDisconnectEvent = new SessionDisconnectEvent(new Object(), message, SESSION_ID, CloseStatus.NORMAL);

        // when
        listener.handleWebSocketDisconnected(sessionDisconnectEvent);

        // then
        verify(playerService).removePlayer(PLAYER_1_NAME);
        verify(matchService).findByPlayer(PLAYER_1_NAME);
        verify(notificationService).notifyPlayer(PLAYER_2_NAME,
                EventFactory.buildDisconnectMessage("Your opponent has left the game."));
    }
}
