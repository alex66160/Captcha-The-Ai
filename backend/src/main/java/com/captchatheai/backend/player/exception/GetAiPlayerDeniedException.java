package com.captchatheai.backend.player.exception;

/**
 * The GetAiPlayerDeniedException is an exception where the player tries to get
 * the AiPlayer when it is not a game result phase.
 * 
 * @author Alex Liu
 */
public class GetAiPlayerDeniedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GetAiPlayerDeniedException(String message) {
		super(message);
	}

}
