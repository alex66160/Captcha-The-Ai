package com.captchatheai.backend.player.exception;

/**
 * The PlayerNotFoundException represents an exception where the player could
 * not be found in a given lobby.
 * 
 * @author Alex Liu
 */
public class PlayerNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PlayerNotFoundException(String message) {
		super(message);
	}

}
