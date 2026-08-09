package com.captchatheai.backend.question.exception;

/**
 * The SendQuestionDeniedException represents an exception where the player
 * tried to send a question while not being in the alive state.
 * 
 * @author Alex Liu
 */
public class SendQuestionDeniedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SendQuestionDeniedException(String message) {
		super(message);
	}

}
