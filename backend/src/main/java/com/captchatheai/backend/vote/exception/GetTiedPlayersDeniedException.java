package com.captchatheai.backend.vote.exception;

/**
 * The GetTiedPlayersDeniedException represents an exception where the player
 * tries to get tied players when the lobby is not in the REVEAL_TIE phase.
 * 
 * @author Alex Liu
 */
public class GetTiedPlayersDeniedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GetTiedPlayersDeniedException(String message) {
		super(message);
	}

}
