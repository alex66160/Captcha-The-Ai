package com.captchatheai.backend.question.exception;

/**
 * The NotQuestionPhaseException represents an exception where the player tries
 * to send a question while the lobby is not in the question phase.
 * 
 * @author Alex Liu
 */
public class NotQuestionPhaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotQuestionPhaseException(String message) {
		super(message);
	}

}
