package com.captchatheai.backend.lobby.exception;

/**
 * The LobbyFullException represents an exception where the player tries to join
 * a lobby that does not exist.
 * 
 * @author Alex Liu
 */
public class LobbyNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LobbyNotFoundException(String message) {
		super(message);
	}
}
