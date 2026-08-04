package com.captchatheai.backend.vote.exception;

/**
 * The AlreadyVotedException represents an exception where the player tries to
 * vote again after already voting.
 * 
 * @author Alex Liu
 */
public class AlreadyVotedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AlreadyVotedException(String message) {
		super(message);
	}
}
