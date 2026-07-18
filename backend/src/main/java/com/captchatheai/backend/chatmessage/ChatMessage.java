package com.captchatheai.backend.chatmessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessage {

	private String message;
	private String playerName;
}
