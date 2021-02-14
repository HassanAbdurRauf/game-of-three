package com.hrauf.got.model;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Event {

    private GameStatus gameStatus;
    private String content;
    private String opponent;
    private String matchId;
    private boolean primaryPlayer;
    private int value;
    private int play;
    private boolean winner;
}
