package com.hrauf.got.service;

import com.hrauf.got.model.Event;
import com.hrauf.got.model.GameInstruction;

public interface GameOfThreeService {
    public Event startGame(String playerName);
    public void processMove(String playerName, GameInstruction gameInstruction);
}
