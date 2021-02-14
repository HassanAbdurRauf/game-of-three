package com.hrauf.got.common;

import com.hrauf.got.model.Match;
import com.hrauf.got.model.Player;

public class TestCommon {
    public static final String PLAYER_1_NAME = "player1";
    public static final String PLAYER_2_NAME = "player2";
    public static final String TEST_MATCH_ID = "test_match";
    public static final Player TEST_PLAYER_1 = new Player(PLAYER_1_NAME);
    public static final Player TEST_PLAYER_2 = new Player(PLAYER_2_NAME);
    public static final Match TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2 = Match.builder().firstPlayer(TEST_PLAYER_1).secondPlayer(TEST_PLAYER_2).matchId("match1").build();
    public static final String USERNAME = "username";
    public static final String SESSION_ID = "sessionId";
}
