package com.hrauf.got.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Match {
    private String matchId;
    private Player firstPlayer;
    private Player secondPlayer;
}
