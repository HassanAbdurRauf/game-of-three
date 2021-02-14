package com.hrauf.got.model;

import lombok.Data;

@Data
public class Player {

    private String username;
    private PlayerStatus status;

    public Player(String username) {
        this.username = username;
        this.status = PlayerStatus.AVAILABLE;
    }
}
