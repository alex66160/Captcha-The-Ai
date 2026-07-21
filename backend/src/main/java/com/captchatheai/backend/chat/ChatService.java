package com.captchatheai.backend.chat;


import java.time.Instant;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.chat.exception.ChatCooldownException;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerState;
import com.captchatheai.backend.player.exception.PlayerDisconnectedException;

import lombok.RequiredArgsConstructor;

/**
 * The ChatService class is for allowing players to send chat messages
 * and making sure it gets broadcasted out to the other players screens.
 * Includes a built in rate limiter to make sure that players
 * can't spam chat messages.
 * 
 * @author Alex Liu
 */
@Service
@RequiredArgsConstructor
public class ChatService {
	

	
	/** Cooldown duration for sending chat messages */
	private static final long CHAT_COOLDOWN_DURATION = 3L;
	
	/** Maximum chat messages to store in the chat history of each lobby */
	private static final int MAX_CHAT_MESSAGES = 20;
	
	/** Lobby service to use */
	private final LobbyService lobbyService;
	
	private final SimpMessagingTemplate messagingTemplate;
	/**
	 * Receives a chat message from a player and checks if they are over the cooldown,
	 * and saves it into that lobby's chat history. Also broadcasts the message
	 * out to other players in the same lobby.
	 * 
	 * @param lobbyId the lobby that the chat message was sent from
	 * @param playerId the player that sent the chat message
	 * @param message the chat message that was sent
	 */
	public void sendChatMessage(String lobbyId, UUID playerId, String message) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			Player player = lobbyService.getPlayerById(lobbyId, playerId);
			if (player.getState() == PlayerState.DISCONNECTED) {
				throw new PlayerDisconnectedException();
			}
		
		// Make sure that the player's chat cooldown has finished
		
			if (player.getLastChatTime() != null && 
					Instant.now().isBefore(player.getLastChatTime().plusSeconds(CHAT_COOLDOWN_DURATION) )) {
				
				throw new ChatCooldownException();
			}
			player.setLastChatTime(Instant.now());
		
		
		
		
		
			Deque<ChatMessage> chatHistory = lobbyService.getLobbyById(lobbyId).getChatHistory();
			
			ChatMessage chatMessage = new ChatMessage(playerId, message);
			
			if (player.getState() == PlayerState.ALIVE || player.getState() == PlayerState.WAITING) {
				if (chatHistory.size() >= MAX_CHAT_MESSAGES) {
					chatHistory.removeFirst();
				} 
				chatHistory.addLast(chatMessage);
		
			}
			
			messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/chat", chatMessage);
			
			if (player.getState() == PlayerState.SPECTATOR) {
				messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/chat/spectator", chatMessage);
			}
		}
	}
	
	
	public void deleteChatHistory(String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			lobby.getChatHistory().clear();
		}
	}

	
}
