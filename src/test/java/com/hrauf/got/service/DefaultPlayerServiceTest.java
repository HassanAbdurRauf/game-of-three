package com.hrauf.got.service;

import com.hrauf.got.repository.PlayerRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;


import java.util.List;
import java.util.Optional;

import static com.hrauf.got.common.TestCommon.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private DefaultPlayerService underTest;

    @Test
    public void whenPlayerIsSaved_Then_DelegateToPlayerStore() {
        // when
        underTest.save(TEST_PLAYER_1);

        // then
        verify(playerRepository).save(TEST_PLAYER_1);
    }


    @Test
    public void removePlayer_PlayerPresent_DelegateToRepository() {
        // given
        Mockito.doNothing().when(playerRepository).delete(any());
        Mockito.when(playerRepository.findByName(PLAYER_1_NAME))
                .thenReturn(Optional.of(TEST_PLAYER_1));

        // when
        underTest.removePlayer(PLAYER_1_NAME);

        //then
        verify(playerRepository).delete(TEST_PLAYER_1);
    }

    @Test
    public void updatePlayerStatusPaired_CurrentlyAvailable_SavingToRepoInvoked() {
        // when
        underTest.updatePlayerStatusPaired(TEST_PLAYER_1);

        verify(playerRepository).save(TEST_PLAYER_1);
    }

    @Test
    public void updatePlayerStatusAvailable_CurrentlyPaired_SavingToRepoInvoked() {
        // when
        underTest.updatePlayerStatusAvailable(TEST_PLAYER_1);

        verify(playerRepository).save(TEST_PLAYER_1);
    }

    @Test
    public void findNewOpponent_OpponentAvailable_ReturnOpponentPlayer() {

        // given
        Mockito.when(playerRepository.getAvailablePlayers())
                .thenReturn(List.of(TEST_PLAYER_1, TEST_PLAYER_2));

        // when
        Assert.assertEquals(TEST_PLAYER_2, underTest.findNewOpponent(TEST_PLAYER_1).get());

        verify(playerRepository).getAvailablePlayers();
    }

    @Test
    public void findNewOpponent_NotOpponentAvailable_ReturnEmptyResponse() {

        // given
        Mockito.when(playerRepository.getAvailablePlayers())
                .thenReturn(List.of(TEST_PLAYER_1));

        // when
        Assert.assertEquals(Optional.empty(), underTest.findNewOpponent(TEST_PLAYER_1));

        verify(playerRepository).getAvailablePlayers();
    }

}
