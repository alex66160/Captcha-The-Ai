package com.captchatheai.backend.lobby;

/**
 * The LobbyIdResponse record represents a lobbyId, and is used for when players
 * initially join a lobby.
 * 
 * @author Alex Liu
 * @param lobbyId the lobbyId that the player joined
 */
public record LobbyIdResponse(int lobbyId) {

}
