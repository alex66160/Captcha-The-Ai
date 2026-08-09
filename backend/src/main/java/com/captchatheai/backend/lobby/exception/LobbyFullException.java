package com.captchatheai.backend.lobby.exception;

/**
 * The LobbyFullException represents an exception where the player tries to join
 * a lobby thats full.
 * 
 * @author Alex Liu
 */
public class LobbyFullException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LobbyFullException(String message) {
		super(message);
	}

}
