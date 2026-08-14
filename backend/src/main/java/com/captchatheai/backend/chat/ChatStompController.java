package com.captchatheai.backend.chat;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.captchatheai.backend.player.PlayerLookup;

import lombok.RequiredArgsConstructor;

/**
 * The ChatStompController allows players to send chat messages to a lobby.
 * 
 * @author Alex Liu
 */
@Controller
@RequiredArgsConstructor
public class ChatStompController {

	private final ChatService chatService;
	private final PlayerLookup playerLookup;

	/**
	 * The sendChatMessage endpoint allows a player to send a chat message to a
	 * given lobby.
	 * 
	 * @param lobbyId                the lobby to send the chat message to
	 * @param accessor               the header accessor that gives us the players
	 *                               sessionId
	 * @param sendChatMessageCommand the command that includes the players chat
	 *                               message
	 */
	@MessageMapping("/lobbies/{lobbyId}/chat-messages")
	public void sendChatMessage(@DestinationVariable int lobbyId, StompHeaderAccessor accessor,
			@Payload SendChatMessageCommand sendChatMessageCommand) {
		String sessionId = accessor.getSessionId();

		chatService.sendChatMessage(lobbyId, playerLookup.getPlayerIdBySessionId(lobbyId, sessionId),
				sendChatMessageCommand.message());
	}

}
