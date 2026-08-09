package com.captchatheai.backend.chat.exception;

/**
 * The InvalidChatMessageException represents an exception where a player sends
 * a chat message thats over 100 characters or is blank.
 * 
 * @author Alex Liu
 */
public class InvalidChatMessageException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidChatMessageException(String message) {
		super(message);
	}

}
