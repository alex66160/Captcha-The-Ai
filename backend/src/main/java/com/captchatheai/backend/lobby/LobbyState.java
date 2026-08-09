package com.captchatheai.backend.lobby;

import java.time.Instant;
import java.util.List;

import com.captchatheai.backend.player.PlayerState;

/**
 * The LobbyState record is a snapshot of the lobby state.
 * 
 * @author Alex Liu
 * @param lobbyPhase   the phase that the lobby is currently in
 * @param phaseEndTime the time at which the current phase is ending
 * @param roundCount   the current round the lobby is on
 * @param players      the list of players
 */
public record LobbyState(LobbyPhase lobbyPhase, Instant phaseEndTime, int roundCount, List<PlayerState> players) {

}
