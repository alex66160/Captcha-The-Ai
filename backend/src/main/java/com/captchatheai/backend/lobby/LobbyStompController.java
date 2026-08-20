package com.captchatheai.backend.lobby;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.captchatheai.backend.player.PlayerLookup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The LobbyStompController class allows players to get the lobby state, join a
 * lobby, join a specific lobby using a lobby id, create a new lobby, and leave
 * a lobby.
 * 
 * @author Alex Liu
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LobbyStompController {

	private final LobbyService lobbyService;
	private final PlayerLookup playerLookup;

	/**
	 * The getLobbyState endpoint sends out the lobby state of a given lobby to a
	 * specific player.
	 * 
	 * @param lobbyId  the lobby to get the lobby state of
	 * @param accessor the header accessor for the player's session id
	 */
	@MessageMapping("/lobbies/{lobbyId}/state")
	public void getLobbyState(@DestinationVariable int lobbyId, StompHeaderAccessor accessor) {
		lobbyService.getLobbyState(lobbyId, accessor.getSessionId());
	}

	/**
	 * The joinLobby endpoint allows a player to join a lobby.
	 * 
	 * @param accessor the header accessor for the player's session id
	 */
	@MessageMapping("/lobbies/join")
	public void joinLobby(StompHeaderAccessor accessor) {
		lobbyService.joinLobby(accessor.getSessionId());
	}

	/**
	 * The joinLobbyById endpoint allows a player to join a specific lobby using a
	 * lobby id. Private lobbies will require a password, public lobbies do not.
	 * 
	 * @param lobbyId              the lobby the player wants to join
	 * @param accessor             the header accessor for the player's session id.
	 * @param joinLobbyByIdRequest the password for the lobby
	 */
	@MessageMapping("/lobbies/{lobbyId}/join")
	public void joinLobbyById(@DestinationVariable int lobbyId, StompHeaderAccessor accessor,
			@Payload JoinLobbyByIdRequest joinLobbyByIdRequest) {
		lobbyService.joinLobbyById(lobbyId, accessor.getSessionId(), joinLobbyByIdRequest.password());
	}

	/**
	 * The createLobby endpoint allows a player to create a new lobby with or
	 * without a password.
	 * 
	 * @param accessor           the header accessor for the player's session id
	 * @param createLobbyRequest the password to be set for the newly created lobby
	 */
	@MessageMapping("/lobbies/create")
	public void createLobby(StompHeaderAccessor accessor, @Payload CreateLobbyRequest createLobbyRequest) {
		log.info("Create lobby was called with session id {}", accessor.getSessionId());
		lobbyService.createLobby(accessor.getSessionId(), createLobbyRequest.password());
	}

	/**
	 * The leaveLobby endpoint allows a player to leave a lobby that they are in.
	 * 
	 * @param lobbyId  the lobby to leave
	 * @param accessor the header accessor to get the player's session id
	 */
	@MessageMapping("/lobbies/{lobbyId}/leave")
	public void leaveLobby(@DestinationVariable int lobbyId, StompHeaderAccessor accessor) {
		String sessionId = accessor.getSessionId();
		UUID playerId = playerLookup.getPlayerIdBySessionId(lobbyId, sessionId);

		lobbyService.leaveLobby(lobbyId, playerId);
	}
}
