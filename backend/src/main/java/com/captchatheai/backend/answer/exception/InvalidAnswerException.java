package com.captchatheai.backend.answer.exception;

/**
 * The InvalidAnswerException represents an exception where the player submitted
 * an answer thats over 100 characters or was blank.
 * 
 * @author Alex Liu
 */
public class InvalidAnswerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidAnswerException(String message) {
		super(message);
	}
}
