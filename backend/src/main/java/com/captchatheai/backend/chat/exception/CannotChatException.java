package com.captchatheai.backend.chat.exception;

/**
 * The CannotChatException represents an exception where a player tries to chat
 * during a lobby phase that does not allow chat.
 * 
 * @author Alex Liu
 */
public class CannotChatException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CannotChatException(String message) {
		super(message);
	}
}
