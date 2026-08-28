package com.captchatheai.backend.question;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * The QuestionRestController class allows players to get the current question
 * from a lobby.
 * 
 * @author Alex Liu
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lobbies")
@CrossOrigin("*")
public class QuestionRestController {

	private final QuestionService questionService;

	/**
	 * The getQuestion endpoint returns the question from a given lobby.
	 * 
	 * @param lobbyId the lobbyId to get the question from
	 * @return the question from the lobby
	 */
	@GetMapping("/{lobbyId}/question")
	public ResponseEntity<QuestionResponse> getQuestion(@PathVariable int lobbyId) {
		return ResponseEntity.ok(questionService.getQuestion(lobbyId));
	}
}
