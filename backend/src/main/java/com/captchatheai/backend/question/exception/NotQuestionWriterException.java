package com.captchatheai.backend.question.exception;

/**
 * The NotQuestionWriterException represents an exception where a player tries
 * to submit a question while they are not the question writer.
 * 
 * @author Alex Liu
 */
public class NotQuestionWriterException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotQuestionWriterException(String message) {
		super(message);
	}

}
