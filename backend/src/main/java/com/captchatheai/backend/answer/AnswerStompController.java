package com.captchatheai.backend.answer;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.captchatheai.backend.player.PlayerService;

import lombok.RequiredArgsConstructor;

/**
 * The AnswerStompController class allows players to send an Answer to a lobby.
 * 
 * 
 * @author Alex Liu
 */
@Controller
@RequiredArgsConstructor
public class AnswerStompController {

	private final AnswerService answerService;

	private final PlayerService playerService;

	/**
	 * The sendAnswer method sends an answer for a player to a given lobby.
	 * 
	 * @param lobbyId   the lobbyId to send to
	 * @param sessionId the sessionId of the player
	 * @param answer    the answer to send
	 */
	@MessageMapping("/lobbies/{lobbyId}/sendAnswer")
	public void sendAnswer(@DestinationVariable int lobbyId, StompHeaderAccessor accessor, String answer) {
		String sessionId = accessor.getSessionId();
		UUID playerId = playerService.getPlayerIdBySessionId(lobbyId, sessionId);

		answerService.sendAnswer(lobbyId, playerId, answer);

	}
}
