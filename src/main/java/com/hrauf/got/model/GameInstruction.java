package com.hrauf.got.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Builder
@Value
public class GameInstruction {

    private String matchId;
    private int randomNumber;
    private int value;
}
