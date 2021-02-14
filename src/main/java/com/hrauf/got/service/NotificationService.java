package com.hrauf.got.service;

import com.hrauf.got.model.Event;

public interface NotificationService {
    public void notifyPlayer(String playerName, Event message);
}
