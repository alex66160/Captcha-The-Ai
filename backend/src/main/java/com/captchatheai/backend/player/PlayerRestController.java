package com.captchatheai.backend.player;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * The PlayerRestController class allows players to get the ai player and
 * eliminated player in a given lobby.
 * 
 * @author Alex Liu
 */
@RestController
@RequestMapping("/api/lobbies")
@RequiredArgsConstructor
public class PlayerRestController {

	private final PlayerService playerService;

	/**
	 * The getAiPlayer endpoint returns the ai player for a given lobby.
	 * 
	 * @param lobbyId the lobbyId to get the ai player from
	 * @return the ai player
	 */
	@GetMapping("/{lobbyId}/aiPlayer")
	public ResponseEntity<AiPlayerResponse> getAiPlayer(@PathVariable int lobbyId) {
		return ResponseEntity.ok(playerService.getAiPlayer(lobbyId));
	}

	/**
	 * The getEliminatedPlayer endpoint returns the eliminated player for a given
	 * lobby.
	 * 
	 * @param lobbyId the lobbyId to get the eliminated player from
	 * @return the eliminated player
	 */
	@GetMapping("/{lobbyId}/eliminatedPlayer")
	public ResponseEntity<EliminatedPlayerResponse> getEliminatedPlayer(@PathVariable int lobbyId) {
		return ResponseEntity.ok(playerService.getEliminatedPlayer(lobbyId));
	}
}
