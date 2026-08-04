package com.captchatheai.backend.chat.exception;

/**
 * The ChatCooldownException represents an exception when a player tries to chat
 * before their chat cooldown has ended.
 * 
 * @author Alex Liu
 */
public class ChatCooldownException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ChatCooldownException(String message) {
		super(message);
	}

}
