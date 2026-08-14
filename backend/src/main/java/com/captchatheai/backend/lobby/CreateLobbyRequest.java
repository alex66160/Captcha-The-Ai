package com.captchatheai.backend.lobby;

/**
 * The CreateLobbyRequest record represents the password the player wants the
 * created lobby to be set to.
 * 
 * @author Alex Liu
 * @param password the password for the newly created lobby
 */
public record CreateLobbyRequest(String password) {

}
