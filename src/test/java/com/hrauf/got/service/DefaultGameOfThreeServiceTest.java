package com.hrauf.got.service;

import com.hrauf.got.exception.OpponentMissingException;
import com.hrauf.got.exception.PlayerNotFoundException;
import com.hrauf.got.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.hrauf.got.common.TestCommon.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultGameOfThreeServiceTest {

    @Mock
    private PlayerService playerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MatchService matchService;

    @InjectMocks
    private DefaultGameOfThreeService gameService;



    @Test(expected = PlayerNotFoundException.class)
    public void startGame_PlayerNotFound_throwException() {
        // given
        Mockito.when(playerService.findPlayer(any())).thenThrow(PlayerNotFoundException.class);

        // when / then
        gameService.startGame(PLAYER_1_NAME);
    }

    @Test
    public void startGame_NoOpponentAvailable_ReturnWaitMessage() {
        // given
        Mockito.when(playerService.findPlayer(any())).thenReturn(new Player(PLAYER_1_NAME));
        Mockito.when(playerService.findNewOpponent(any())).thenReturn(Optional.empty());

        // when
        var message = gameService.startGame(PLAYER_1_NAME);

        assertThat(message.getGameStatus()).isEqualTo(GameStatus.WAITING);
        assertThat(message.getContent()).isEqualTo("Waiting for available player");
        Mockito.verifyNoInteractions(matchService);
        Mockito.verifyNoInteractions(notificationService);
    }


    @Test
    public void startGame_OpponentAvailable_NotifyOpponentUpdateAndPlayerStatus() {
        // given
        Mockito.when(playerService.findPlayer(PLAYER_1_NAME)).thenReturn(TEST_PLAYER_1);
        Mockito.when(playerService.findNewOpponent(TEST_PLAYER_1)).thenReturn(Optional.of(TEST_PLAYER_2));
        Mockito.when(matchService.createMatch(TEST_PLAYER_1, TEST_PLAYER_2)).thenReturn(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2);

        // when
        var message = gameService.startGame(PLAYER_1_NAME);

        // then
        Assert.assertEquals(message.getMatchId(), TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2.getMatchId());
        Assert.assertEquals(message.getGameStatus(), GameStatus.START);
        Assert.assertEquals(message.getOpponent(), TEST_PLAYER_2.getUsername());

        Mockito.verify(notificationService).notifyPlayer(TEST_PLAYER_2.getUsername(), EventFactory.buildStartMessage(TEST_PLAYER_1, TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2));

        verify(playerService).findNewOpponent(TEST_PLAYER_1);
        verify(playerService, times(2)).updatePlayerStatusPaired(any());

    }

    @Test(expected = PlayerNotFoundException.class)
    public void processMove_PlayerNotFound_ThrowException() {
        // given
        Mockito.when(playerService.findPlayer(PLAYER_1_NAME)).thenThrow(PlayerNotFoundException.class);
        // when /then
        gameService.processMove(PLAYER_1_NAME, GameInstruction.builder().matchId(TEST_MATCH_ID).randomNumber(56).build());
    }


    @Test
    public void processMove_FirstMoveOfGame_NotifyOpponent() {
        // given
        Mockito.when(playerService.findPlayer(PLAYER_1_NAME)).thenReturn(TEST_PLAYER_1);
        Mockito.when(matchService.find(TEST_MATCH_ID)).thenReturn(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2);
        Mockito.when(matchService.getOpponent(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2, PLAYER_1_NAME))
                .thenReturn(TEST_PLAYER_2);

        // when
        gameService.processMove(PLAYER_1_NAME, GameInstruction.builder().matchId(TEST_MATCH_ID).randomNumber(56).build());

        // then
        verify(notificationService).notifyPlayer(PLAYER_2_NAME, Event.builder()
                .gameStatus(GameStatus.PLAY)
                .value(56)
                .build());
    }

    @Test
    public void processMove_NumberIsNotDivisibleByDivisor_ThrowInvalidMoveException() {
        // given
        Mockito.when(playerService.findPlayer(PLAYER_1_NAME)).thenReturn(TEST_PLAYER_1);
        Mockito.when(matchService.find(TEST_MATCH_ID)).thenReturn(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2);
        Mockito.when(matchService.getOpponent(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2, PLAYER_1_NAME))
                .thenReturn(TEST_PLAYER_2);

        // when / then
        gameService.processMove(PLAYER_1_NAME, GameInstruction.builder().matchId(TEST_MATCH_ID).value(99).build());
    }

    @Test(expected = PlayerNotFoundException.class)
    public void processPlayerMove_PlayerNotFound_ExceptionIsThrown() {
        // given
        Mockito.when(playerService.findPlayer(PLAYER_1_NAME)).thenThrow(PlayerNotFoundException.class);

        // when / then
        gameService.processMove(PLAYER_1_NAME, GameInstruction.builder().matchId(TEST_MATCH_ID).value(99).build());
    }


    @Test(expected = OpponentMissingException.class)
    public void processPlayerMove_ShouldFailIfOpponentDoesNotExist() {
        // given
        Mockito.when(playerService.findPlayer(PLAYER_1_NAME)).thenReturn(TEST_PLAYER_1);
        Mockito.when(matchService.find(TEST_MATCH_ID)).thenReturn(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2);
        Mockito.when(matchService.getOpponent(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2, PLAYER_1_NAME))
                .thenReturn(null);

        // when / then
        gameService.processMove(PLAYER_1_NAME, GameInstruction.builder().matchId(TEST_MATCH_ID).value(99).build());
    }

    @Test
    public void processMove_GameNotEndingState_GameStatusUnChanged() {
        // given
        Mockito.when(playerService.findPlayer(PLAYER_1_NAME)).thenReturn(TEST_PLAYER_1);
        Mockito.when(matchService.find(TEST_MATCH_ID)).thenReturn(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2);
        Mockito.when(matchService.getOpponent(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2, PLAYER_1_NAME))
                .thenReturn(TEST_PLAYER_2);

        // when
        gameService.processMove(PLAYER_1_NAME, GameInstruction.builder().matchId(TEST_MATCH_ID).value(21).build());

        // then
        verify(notificationService).notifyPlayer(PLAYER_2_NAME, Event.builder()
                .gameStatus(GameStatus.PLAY)
                .value(7)
                .build());
    }


    @Test
    public void processMove_WinningCondition_ShouldNotifyBoth() {
        //when
        Mockito.when(playerService.findPlayer(PLAYER_1_NAME)).thenReturn(TEST_PLAYER_1);
        Mockito.when(matchService.find(TEST_MATCH_ID)).thenReturn(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2);
        Mockito.when(matchService.getOpponent(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2, PLAYER_1_NAME))
                .thenReturn(TEST_PLAYER_2);

        // when
        gameService.processMove(PLAYER_1_NAME, GameInstruction.builder().matchId(TEST_MATCH_ID).value(3).build());

        // then
        verify(notificationService).notifyPlayer(PLAYER_2_NAME, EventFactory.buildGameOverMessage(false));
        verify(notificationService).notifyPlayer(PLAYER_1_NAME, EventFactory.buildGameOverMessage(true));
    }

}
