package com.captchatheai.backend.question.exception;

/**
 * The InvalidQuestionException represents an exception where the player sends a
 * question that is over 100 characters or is blank.
 * 
 * @author Alex Liu
 */
public class InvalidQuestionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidQuestionException(String message) {
		super(message);
	}

}
