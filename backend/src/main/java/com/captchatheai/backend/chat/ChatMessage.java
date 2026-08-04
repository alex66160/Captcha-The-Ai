package com.captchatheai.backend.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * The ChatMessage model is used to store the playerName, message, and the time
 * it was sent, which is the time between when the lobby started and when the
 * message was sent.
 * 
 * @author Alex Liu
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ChatMessage {

	private final String playerName;
	private final String message;
	private final long sentTime;
}
