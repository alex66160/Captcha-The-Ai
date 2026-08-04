package com.captchatheai.backend.answer.exception;

/**
 * The CannotAnswerAsQuestionWriterException represents an exception where the
 * player tries to send an answer as a question writer.
 * 
 * @author Alex Liu
 */
public class CannotAnswerAsQuestionWriterException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CannotAnswerAsQuestionWriterException(String message) {
		super(message);
	}
}
