package com.captchatheai.backend.lobby;

/**
 * The StatsBroadcast record represents the total player count and total lobby
 * count of the server.
 * 
 * @author Alex Liu
 * @param totalPlayerCount the total players in the server
 * @param totalLobbyCount  the total lobbies in the server
 */
public record StatsBroadcast(int totalPlayerCount, int totalLobbyCount) {

}
