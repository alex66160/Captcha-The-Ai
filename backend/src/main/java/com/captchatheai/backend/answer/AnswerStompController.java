package com.captchatheai.backend.answer;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.captchatheai.backend.player.PlayerLookup;

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

	private final PlayerLookup playerLookup;

	/**
	 * The sendAnswer endpoint sends an answer for a player to a given lobby.
	 * 
	 * @param lobbyId             the lobbyId to send to
	 * @param accessor            the header accessor to get the sessionId of the
	 *                            player
	 * @param submitAnswerRequest the request that contains the answer of the player
	 */
	@MessageMapping("/lobbies/{lobbyId}/answers")
	public void sendAnswer(@DestinationVariable int lobbyId, StompHeaderAccessor accessor,
			@Payload SubmitAnswerRequest submitAnswerRequest) {
		String sessionId = accessor.getSessionId();
		UUID playerId = playerLookup.getPlayerIdBySessionId(lobbyId, sessionId);

		answerService.sendAnswer(lobbyId, playerId, submitAnswerRequest.answer());

	}
}
