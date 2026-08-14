package com.captchatheai.backend.lobby;

/**
 * The LobbyPhaseExpiredEvent record represents a lobby where its phase has
 * expired.
 * 
 * @author Alex Liu
 * @param lobbyId the lobby with the expired phase
 */
public record LobbyPhaseExpiredEvent(int lobbyId) {

}
