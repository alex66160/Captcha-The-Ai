package com.captchatheai.backend.answer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * The AnswerRestController class allows users to get the answers in a lobby.
 * 
 * @author Alex Liu
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lobbies")
@CrossOrigin("*")
public class AnswerRestController {

	private final AnswerService answerService;

	/**
	 * The getAnswers endpoint returns all the answers in a given lobby.
	 * 
	 * @param lobbyId the lobbyId to get the answers from
	 * @return the answers from the lobby
	 */
	@GetMapping("/{lobbyId}/answers")
	public ResponseEntity<AnswersResponse> getAnswers(@PathVariable int lobbyId) {
		return ResponseEntity.ok(answerService.getAnswers(lobbyId));
	}

}
