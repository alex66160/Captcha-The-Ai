package com.captchatheai.backend.question.exception;

/**
 * The GetQuestionDeniedException class represents an exception where the player
 * tries to get the question during an invalid lobby phase.
 * 
 * @author Alex Liu
 */
public class GetQuestionDeniedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GetQuestionDeniedException(String message) {
		super(message);
	}

}
