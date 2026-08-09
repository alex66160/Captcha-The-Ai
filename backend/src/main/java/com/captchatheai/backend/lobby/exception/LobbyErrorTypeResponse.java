package com.captchatheai.backend.lobby.exception;

/**
 * The LobbyErrorTypeResponse record is used to indicate why joining a lobby has
 * failed to the frontend.
 * 
 * @author Alex Liu
 * @param lobbyErrorType the reason why the lobby join has failed
 */
public record LobbyErrorTypeResponse(LobbyErrorType lobbyErrorType) {

}
