package com.captchatheai.backend.answer.exception;

/**
 * The GetAnswersDeniedException represents an exception when a
 * player tries to get answers for a lobby when its not in a DISCUSS
 * or VOTING phase.
 * 
 * @author Alex Liu
 */
public class GetAnswersDeniedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GetAnswersDeniedException(String message) {
		super(message);
	}
}
