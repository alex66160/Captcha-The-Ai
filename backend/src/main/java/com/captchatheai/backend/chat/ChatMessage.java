package com.captchatheai.backend.chat;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessage {
	
	private UUID playerId;
	private String message;
	
}
