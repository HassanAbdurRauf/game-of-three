package com.hrauf.got.service;

import com.hrauf.got.model.CommonConstants;
import com.hrauf.got.model.EventFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RunWith(MockitoJUnitRunner.class)
public class DefaultNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private DefaultNotificationService service;


    @Test
    public void notifyPlayer_Should_SendMessageToPlayer() {
        // given
        var playerName = "test_player_name";
        var gameMessage = EventFactory.buildPlayMessage(99);

        // then
        service.notifyPlayer(playerName, gameMessage);

        // then
        Mockito.verify(messagingTemplate).convertAndSendToUser(playerName, CommonConstants.MESSAGE_QUEUE_EVENTS_PATH, gameMessage);
    }
}
