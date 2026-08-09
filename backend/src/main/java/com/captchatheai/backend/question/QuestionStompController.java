package com.captchatheai.backend.question;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.captchatheai.backend.player.PlayerService;

import lombok.RequiredArgsConstructor;

/**
 * The QuestionStompController class lets players send a question to a lobby.
 * 
 * @author Alex Liu
 */
@Controller
@RequiredArgsConstructor
public class QuestionStompController {

	private final QuestionService questionService;

	private final PlayerService playerService;

	/**
	 * The sendQuestion endpoint allows players to send their questions.
	 * 
	 * @param lobbyId               the lobbyId to send the question to
	 * @param accessor              the header accessor to get the sessionId
	 * @param submitQuestionRequest the request containing the question to be sent
	 */
	@MessageMapping("/lobbies/{lobbyId}/sendQuestion")
	public void sendQuestion(@DestinationVariable int lobbyId, StompHeaderAccessor accessor,
			SubmitQuestionRequest submitQuestionRequest) {
		String sessionId = accessor.getSessionId();
		questionService.sendQuestion(lobbyId, playerService.getPlayerIdBySessionId(lobbyId, sessionId),
				submitQuestionRequest.question());

	}
}
