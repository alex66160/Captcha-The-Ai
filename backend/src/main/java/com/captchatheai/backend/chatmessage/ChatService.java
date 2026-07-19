package com.captchatheai.backend.chatmessage;


import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.PlayerDisconnectedException;
import com.captchatheai.backend.player.PlayerState;

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
	
	/** Map to store when a player last sent a chat message */
	private final Map<UUID, Instant> lastSentMessage = new ConcurrentHashMap<>();
	
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
		PlayerState playerState = lobbyService.getPlayerById(lobbyId, playerId).getState();
		if (playerState == PlayerState.DISCONNECTED) {
			throw new PlayerDisconnectedException();
		}
		
		// Make sure that the player's chat cooldown has finished
		synchronized (lastSentMessage) {
			if (lastSentMessage.get(playerId) != null && 
					Instant.now().isBefore(lastSentMessage.get(playerId).plusSeconds(CHAT_COOLDOWN_DURATION) )) {
				
				throw new ChatCooldownException();
			}
			lastSentMessage.put(playerId, Instant.now());
		}
		
		
		
		
		Deque<ChatMessage> chatHistory = lobbyService.getLobbyById(lobbyId).getChatHistory();
		
		ChatMessage chatMessage = new ChatMessage(playerId, message);
		
		if (playerState == PlayerState.ALIVE) {
			// Synchronize chatHistory to make sure that the max chat message limit
			// is never exceeded
			synchronized (chatHistory) {
				
				if (chatHistory.size() >= MAX_CHAT_MESSAGES) {
					chatHistory.removeFirst();
				} 
				chatHistory.addLast(chatMessage);
			}
			
			
		}
		
		messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/chat", chatMessage);
		
		if (playerState == PlayerState.SPECTATOR) {
			messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/chat/spectator", chatMessage);
		}
	}
	
	/** 
	 * Deletes the given entry for a playerId in the chatRateLimiter map 
	 * to free up space when a player disconnects or leaves from a lobby. 
	 * 
	 * @param playerId the playerId to remove from the rate limiter
	 */
	public void deleteRateLimiterEntry(UUID playerId) {
		lastSentMessage.remove(playerId);
	}
	

	
}
