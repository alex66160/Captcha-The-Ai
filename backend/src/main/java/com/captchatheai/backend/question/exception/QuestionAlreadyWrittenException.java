package com.captchatheai.backend.question.exception;

/**
 * The QuestionAlreadyWrittenException represents an exception where the
 * question writer tries to submit another question after they already submitted
 * one.
 * 
 * @author Alex Liu
 * 
 */
public class QuestionAlreadyWrittenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public QuestionAlreadyWrittenException(String message) {
		super(message);
	}

}
