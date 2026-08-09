package com.captchatheai.backend.lobby.exception;

/**
 * The IncorrectLobbyPasswordException represents an exception where a player
 * has entered the incorrect password for a lobby.
 * 
 * @author Alex Liu
 */
public class IncorrectLobbyPasswordException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IncorrectLobbyPasswordException(String message) {
		super(message);
	}
}
