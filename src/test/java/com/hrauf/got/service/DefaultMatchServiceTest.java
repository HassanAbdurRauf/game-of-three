package com.hrauf.got.service;

import com.hrauf.got.exception.MatchNotFoundException;
import com.hrauf.got.repository.MatchRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.hrauf.got.common.TestCommon.*;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMatchServiceTest {

    @Mock
    MatchRepository matchRepository;

    @InjectMocks
    DefaultMatchService underTest;

    @Test
    public void createMatch_BothPlayersAvailable_ReturnMatch() {
        // given
        Mockito.doNothing().when(matchRepository).save(any());

        // when
        underTest.createMatch(TEST_PLAYER_1, TEST_PLAYER_2);

        // then
        Mockito.verify(matchRepository).save(any());
    }

    @Test
    public void endMatch_ShouldInvokeRepoDelete() {
        // given
        Mockito.doNothing().when(matchRepository).remove(any());

        // when
        underTest.endMatch(TEST_MATCH_ID);

        // then
        Mockito.verify(matchRepository).remove(any());
    }

    @Test
    public void findById_MatchInProgress_ReturnMatch() {
        // given
        Mockito.when(matchRepository.findById(TEST_MATCH_ID)).thenReturn(Optional.ofNullable(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2));

        // when /then
        Assert.assertEquals(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2, underTest.find(TEST_MATCH_ID));
        Mockito.verify(matchRepository).findById(TEST_MATCH_ID);
    }

    @Test(expected = MatchNotFoundException.class)
    public void findById_MatchNotPresent_MatchNotFoundExceptionThrown() {
        // given
        Mockito.when(matchRepository.findById(TEST_MATCH_ID)).thenReturn(java.util.Optional.empty());

        // when / then
        underTest.find(TEST_MATCH_ID);
    }

    @Test
    public void findByPlayer_MatchNotPresent_ReturnEmptyResponse() {
        // given
        Mockito.when(matchRepository.findByPlayer(PLAYER_1_NAME)).thenReturn(java.util.Optional.empty());

        // when /then
        Assert.assertEquals(Optional.empty(), underTest.findByPlayer(PLAYER_1_NAME));
    }

    @Test
    public void findByPlayer_MatchInProgress_ReturnMatch() {
        // given
        Mockito.when(matchRepository.findByPlayer(PLAYER_1_NAME))
                .thenReturn(Optional.of(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2));

        // when /then
        Assert.assertEquals(Optional.of(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2),
                underTest.findByPlayer(PLAYER_1_NAME));
    }

    @Test
    public void getOpponent_MatchInProgress_ReturnOpponent() {
        // given
        Mockito.when(matchRepository.findByPlayer(PLAYER_1_NAME))
                .thenReturn(Optional.of(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2));

        // when /then
        Assert.assertEquals(Optional.of(TEST_MATCH_BETWEEN_PLAYER_1_AND_PLAYER_2),
                underTest.findByPlayer(PLAYER_1_NAME));
    }

}
