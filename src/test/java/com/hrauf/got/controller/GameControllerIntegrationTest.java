package com.hrauf.got.controller;

import com.hrauf.got.GameOfThreeApplication;
import com.hrauf.got.exception.PlayerNotFoundException;
import com.hrauf.got.model.Event;
import com.hrauf.got.model.GameStatus;
import com.hrauf.got.service.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.*;
import static com.hrauf.got.common.TestCommon.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GameOfThreeApplication.class)
@WebAppConfiguration
public class GameControllerIntegrationTest {

    static final int PORT = 8080;
    static final String gameURL = String.format("ws://localhost:%d/game-of-three", PORT);

    @Mock
    private DefaultGameOfThreeService gameService;

    @Mock
    private DefaultPlayerService playerService;

    @Mock
    private DefaultMatchService matchService;

    private WebSocketStompClient stompClient;

    private CompletableFuture<Object> completableFuture;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        stompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
        completableFuture = new CompletableFuture<>();
    }


    @Test(expected = ExecutionException.class)
    public void connect_UserNameNotProvided_ThrowException() throws ExecutionException, InterruptedException {
        // when / then
        stompClient.connect(gameURL, new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                assertThat(headers.getFirst("message")).isEqualTo("username is required to establish a connection");
            }
        }).get();
    }

    @Test(expected = ExecutionException.class)
    public void connect_UserNameAlreadyExist_ThrowException() throws Exception {
        // given
        var stompHeaders = new StompHeaders();
        stompHeaders.add("username", PLAYER_1_NAME);
        Mockito.when(playerService.exists(PLAYER_1_NAME)).thenReturn(true);

        // when
        stompClient.connect(gameURL, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                assertThat(headers.getFirst("message")).isEqualTo("Player with username already connected!!");
            }
        }).get();

        //then
        verify(playerService).exists(PLAYER_1_NAME);
    }

    @Test
    public void startGame_ShouldCallStartGameService() throws Exception {

        // given
        Mockito.when(gameService.startGame(any())).thenReturn(Event.builder().gameStatus(GameStatus.WAITING).build());

        // when
        StompSession stompSession = createSession(new MappingJackson2MessageConverter());

        stompSession.subscribe("/user/mq/events", new TestStompFrameHandler(Event.class));
        stompSession.send("/app/game.start", null);

        // then
        var message = (Event) completableFuture.get(3, SECONDS);
        Assert.assertEquals(GameStatus.WAITING, message.getGameStatus());
    }


    @Test
    public void startGame_NoOpponentAvailable_ShouldAddErrorMessageToErrorQueue() throws Exception {
        // given
        Mockito.when(gameService.startGame(any())).thenThrow(new PlayerNotFoundException("No player currently available."));

        // when
        StompSession stompSession = createSession(new StringMessageConverter());
        stompSession.subscribe("/user/mq/errors", new TestStompFrameHandler(String.class));
        stompSession.send("/app/game.start", null);

        // then
        String errorMessage = (String) completableFuture.get(3, SECONDS);
        Assert.assertEquals("No player currently available.", errorMessage);
    }

    private StompSession createSession(MessageConverter messageConverter) throws Exception {
        var stompHeaders = new StompHeaders();
        stompHeaders.add("username", PLAYER_1_NAME);

        stompClient.setMessageConverter(messageConverter);
        return stompClient.connect(gameURL, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                exception.printStackTrace();
            }
        }).get(1, SECONDS);
    }

    class TestStompFrameHandler implements StompFrameHandler {

        private final Class<?> aClass;

        public TestStompFrameHandler(Class aClass) {
            this.aClass = aClass;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return aClass;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            completableFuture.complete(payload);
        }
    }


}
