package com.captchatheai.backend.player.exception;

/**
 * The PlayerDisconnectedException represents an exception where a player is
 * removed after their status has already been set to disconnected.
 * 
 * @author Alex Liu
 */
public class PlayerDisconnectedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PlayerDisconnectedException(String message) {
		super(message);
	}

}
