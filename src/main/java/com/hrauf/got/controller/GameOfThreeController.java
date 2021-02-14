package com.hrauf.got.controller;

import com.hrauf.got.model.GameInstruction;
import com.hrauf.got.model.Event;
import com.hrauf.got.config.WebSocketConfig;

import com.hrauf.got.service.GameOfThreeService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

import static com.hrauf.got.model.CommonConstants.MESSAGE_QUEUE_EVENTS_PATH;
import static com.hrauf.got.model.CommonConstants.GAME_START_PATH;
import static com.hrauf.got.model.CommonConstants.MAKE_MOVE_PATH;
import static com.hrauf.got.model.CommonConstants.MESSAGE_QUEUE_ERRORS_PATH;

@Controller
@AllArgsConstructor
public class GameOfThreeController {

    private final GameOfThreeService gameOfThreeService;

    @MessageMapping(GAME_START_PATH)
    @SendToUser(MESSAGE_QUEUE_EVENTS_PATH)
    public Event startGame(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put(WebSocketConfig.USERNAME_HEADER, principal.getName());
        return gameOfThreeService.startGame(principal.getName());
    }

    @MessageMapping(MAKE_MOVE_PATH)
    public void makeMove(GameInstruction gameInstruction, Principal principal) {
        gameOfThreeService.processMove(principal.getName(), gameInstruction);
    }

    @MessageExceptionHandler
    @SendToUser(MESSAGE_QUEUE_ERRORS_PATH)
    public String handleException(Throwable throwable) {
        return throwable.getMessage();
    }
}
