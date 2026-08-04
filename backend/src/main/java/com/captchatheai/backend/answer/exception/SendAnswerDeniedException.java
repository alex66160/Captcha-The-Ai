package com.captchatheai.backend.answer.exception;

/**
 * The SendAnswerDeniedException represents an exception where the player tries
 * to send an answer while not being in a valid player state.
 * 
 * @author Alex Liu
 * 
 */
public class SendAnswerDeniedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SendAnswerDeniedException(String message) {
		super(message);
	}

}
