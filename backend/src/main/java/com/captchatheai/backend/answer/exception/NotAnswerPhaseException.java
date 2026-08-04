package com.captchatheai.backend.answer.exception;

/**
 * The NotAnswerPhaseException represents an exception when a player
 * tries to send an answer while not in the answer phase.
 * 
 * @author Alex Liu
 */
public class NotAnswerPhaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NotAnswerPhaseException(String message) {
		super(message);
	}

}
