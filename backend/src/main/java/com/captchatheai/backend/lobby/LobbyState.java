package com.captchatheai.backend.lobby;

import java.time.Instant;
import java.util.List;

import com.captchatheai.backend.player.PlayerState;

public record LobbyState(LobbyPhase lobbyPhase, Instant phaseEndTime, int roundCount, List<PlayerState> players) {

}
