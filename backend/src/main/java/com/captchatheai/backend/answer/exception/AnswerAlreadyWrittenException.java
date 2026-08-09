package com.captchatheai.backend.answer.exception;

/**
 * The AnswerAlreadyWrittenException represents an exception where a player
 * tries to send an answer after they have already answered.
 * 
 * @author Alex Liu
 */
public class AnswerAlreadyWrittenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AnswerAlreadyWrittenException(String message) {
		super(message);
	}

}
