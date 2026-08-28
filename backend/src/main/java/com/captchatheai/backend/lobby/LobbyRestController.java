package com.captchatheai.backend.lobby;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * The LobbyRestController class lets a player know if a lobby they joined
 * through entering a lobby id is password protected.
 * 
 * @author Alex Liu
 */
@RestController
@RequestMapping("/api/lobbies")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LobbyRestController {

	private final LobbyService lobbyService;

	/**
	 * The getIsLobbyPasswordProtected endpoint checks if a lobby with a given lobby
	 * id is password protected.
	 * 
	 * @param lobbyId the lobby the player wants to check
	 * @return whether or not the lobby is password protected
	 */
	@GetMapping("/{lobbyId}/is-password-protected")
	public ResponseEntity<IsLobbyPasswordProtectedResponse> getIsLobbyPasswordProtected(@PathVariable int lobbyId) {

		return ResponseEntity.ok(lobbyService.getIsLobbyPasswordProtected(lobbyId));

	}
}
