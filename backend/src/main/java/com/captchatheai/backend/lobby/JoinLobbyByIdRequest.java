package com.captchatheai.backend.lobby;

/**
 * The JoinLobbyByIdRequest record represents the password sent over to join a
 * given lobby.
 * 
 * @author Alex Liu
 * @param password the password for the lobby
 */
public record JoinLobbyByIdRequest(String password) {

}
