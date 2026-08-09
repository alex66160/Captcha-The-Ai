package com.captchatheai.backend.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.chat.exception.CannotChatException;
import com.captchatheai.backend.chat.exception.ChatCooldownException;
import com.captchatheai.backend.chat.exception.InvalidChatMessageException;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The ChatService class allows players to send chat messages, broadcasts it to
 * other players, stores messages, and also allows the deletion of chat
 * messages.
 * 
 * @author Alex Liu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	/** Cooldown duration for sending chat messages */
	private static final long CHAT_COOLDOWN_DURATION = 3;

	/** Maximum chat messages to store in the chat history of each lobby */
	private static final int MAX_CHAT_MESSAGES = 20;

	/** Maximum length for a chat message */
	private static final int MAX_CHAT_LENGTH = 100;

	private final LobbyService lobbyService;

	private final PlayerService playerService;

	private final SimpMessagingTemplate messagingTemplate;

	/**
	 * The sendChatMessage method receives a chat message from a player and checks
	 * if they are over the cooldown, and saves it into that lobby's chat history.
	 * Also broadcasts the message out to other players in the same lobby.
	 * 
	 * @param lobbyId  the lobby that the chat message was sent from
	 * @param playerId the player that sent the chat message
	 * @param message  the chat message that was sent
	 * @throws CannotChatException         if the lobby is not in a valid chat phase
	 *                                     or player is not in a valid player state
	 * @throws InvalidChatMessageException if the chat message is over 100
	 *                                     characters or is blank
	 * @throws ChatCoolDownException       if the player's cooldown has not ended
	 *                                     yet
	 */
	public void sendChatMessage(int lobbyId, UUID playerId, String message) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {

			Player player = playerService.getPlayerById(lobbyId, playerId);

			PlayerStatus playerStatus = player.getStatus();

			if (playerStatus == PlayerStatus.DISCONNECTED || playerStatus == PlayerStatus.HIDDEN) {
				throw new CannotChatException(
						"Lobby Id: " + lobbyId + ", Player State: " + playerStatus + ", Player Id: " + playerId
								+ ", Send Chat Denied: Player cannot chat if disconnected or hidden.");
			}

			if (lobby.getPhase() == LobbyPhase.INTERMISSION || lobby.getPhase() == LobbyPhase.STARTING) {
				throw new CannotChatException(
						"Lobby Id: " + lobbyId + ", Lobby Phase: " + lobby.getPhase() + ", Player Id: " + playerId
								+ ", Send Chat Denied: Player cannot chat during INTERMISSION or STARTING phase.");
			}

			if (message == null || message.length() > MAX_CHAT_LENGTH || message.isBlank()) {
				throw new InvalidChatMessageException("Lobby Id: " + lobbyId + ", Player Id: " + playerId
						+ ", Send Chat Denied: Message is over " + MAX_CHAT_LENGTH + " characters or is blank.");
			}
			// Make sure that the player's chat cooldown has finished
			if (player.getLastChatTime() != null
					&& Instant.now().isBefore(player.getLastChatTime().plusSeconds(CHAT_COOLDOWN_DURATION))) {

				throw new ChatCooldownException("Lobby Id: " + lobbyId + ", Player Id: " + playerId
						+ ", Send Chat Denied: Player cooldown has not ended yet.");
			}
			player.setLastChatTime(Instant.now());

			Deque<ChatMessage> chatHistory = lobbyService.getLobbyById(lobbyId).getChatHistory();

			ChatMessage chatMessage = new ChatMessage(player.getName(), message,
					Duration.between(lobby.getGameStartTime(), Instant.now()).toSeconds());

			SentChatMessageEvent chatMessageSentEvent = new SentChatMessageEvent(player.getName(), message);

			// Only store chat messages for players that are alive.
			if (playerStatus == PlayerStatus.ALIVE) {
				if (chatHistory.size() >= MAX_CHAT_MESSAGES) {
					chatHistory.removeFirst();
				}
				chatHistory.addLast(chatMessage);
				// Broadcast messages that are made by alive players to the default chat, but do
				// not broadcast messages made by spectators to the default chat.
				messagingTemplate.convertAndSend("/topic/lobbies/" + lobbyId + "/chat", chatMessageSentEvent);
			}

			// Messages that are made by either alive players or spectators should be seen
			// by spectators.
			messagingTemplate.convertAndSend("/topic/lobbies/" + lobbyId + "/chat/spectator", chatMessageSentEvent);

			log.info("Lobby Id: {}, Player Id: {}, Player State: {}, Chat message was successfully sent out.", lobbyId,
					playerId, playerStatus);
		}
	}

	/**
	 * The deleteChatHistory method deletes the chat history for a given lobby.
	 * 
	 * @param lobbyId the lobby to delete messages from
	 */
	public void deleteChatHistory(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			lobby.getChatHistory().clear();
			log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Chat history was successfully deleted.", lobbyId,
					lobby.getRoundCount(), lobby.getPhase());
		}
	}

}
