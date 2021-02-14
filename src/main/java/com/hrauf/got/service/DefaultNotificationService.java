package com.hrauf.got.service;

import com.hrauf.got.model.CommonConstants;
import com.hrauf.got.model.Event;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Component
public class DefaultNotificationService implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyPlayer(String playerName, Event message) {
        messagingTemplate.convertAndSendToUser(playerName, CommonConstants.MESSAGE_QUEUE_EVENTS_PATH, message);
    }
}
