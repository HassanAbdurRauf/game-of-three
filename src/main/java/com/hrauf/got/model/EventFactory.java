package com.hrauf.got.model;

public class EventFactory {

    public static final String WAITING_MESSAGE = "Waiting for available player";
    public static final String OPPONENT_MESSAGE = "Your opponent is %s ";
    public static final String DISCONNECT_MESSAGE = "%s disconnected from game";

    public static Event buildWaitingMessage() {
        return Event.builder()
                .gameStatus(GameStatus.WAITING)
                .primaryPlayer(true)
                .content(WAITING_MESSAGE)
                .build();
    }

    public static Event buildPlayMessage(int value) {
        return Event.builder()
                .gameStatus(GameStatus.PLAY)
                .value(value)
                .build();
    }

    public static Event buildGameOverMessage(boolean winner) {
        return Event.builder()
                .gameStatus(GameStatus.GAMEOVER)
                .winner(winner)
                .build();
    }

    public static Event buildStartMessage(Player player, Match match) {
        return Event
                .builder()
                .matchId(match.getMatchId())
                .opponent(player.getUsername())
                .primaryPlayer(match.getFirstPlayer().getUsername().equals(player.getUsername()))
                .gameStatus(GameStatus.START)
                .content(String.format(OPPONENT_MESSAGE, player.getUsername()))
                .build();
    }

    public static Event buildDisconnectMessage(String disconnectedPlayerName) {
        return Event.builder()
                .gameStatus(GameStatus.DISCONNECT)
                .content(String.format(DISCONNECT_MESSAGE, disconnectedPlayerName))
                .build();
    }
}
