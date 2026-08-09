package com.captchatheai.backend.vote.exception;

/**
 * The VotingDeniedException represents an exception where the player tries to
 * vote during an invalid lobby phase, or the player itself is not alive, or the
 * player to receive the vote is not alive.
 * 
 * @author Alex Liu
 */
public class VotingDeniedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VotingDeniedException(String message) {
		super(message);
	}

}
