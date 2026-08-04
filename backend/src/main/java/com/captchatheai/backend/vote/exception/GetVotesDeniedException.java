package com.captchatheai.backend.vote.exception;

/**
 * The GetVotesDeniedException represents an exception when a player tries to
 * get the votes when the lobby is not in the reveal phase.
 * 
 * @author Alex Liu
 */
public class GetVotesDeniedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GetVotesDeniedException(String message) {
		super(message);
	}

}
