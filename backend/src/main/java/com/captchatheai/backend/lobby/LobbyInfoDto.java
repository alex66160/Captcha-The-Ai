package com.captchatheai.backend.lobby;

import java.time.Instant;

public record LobbyInfoDto (LobbyPhase lobbyPhase, Instant phaseEndTime, int roundCount){

}
