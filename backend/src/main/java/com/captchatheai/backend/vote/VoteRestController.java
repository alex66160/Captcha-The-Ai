package com.captchatheai.backend.vote;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * The VoteRestController allows players to get votes for a lobby and get the
 * tied players in a lobby.
 * 
 * @author Alex Liu
 */
@RestController
@RequestMapping("/api/lobbies")
@RequiredArgsConstructor
public class VoteRestController {

	private final VoteService voteService;

	/**
	 * The getVotes endpoint allows players to get the votes for a lobby.
	 * 
	 * @param lobbyId the lobbyId to get the votes from
	 * @return the votes from a lobby
	 */
	@GetMapping("/{lobbyId}/votes")
	public ResponseEntity<VotesResponse> getVotes(@PathVariable int lobbyId) {
		return ResponseEntity.ok(voteService.getVotes(lobbyId));

	}

	/**
	 * The getTiedPlayers endpoint allows players to get the tied players from a
	 * lobby.
	 * 
	 * @param lobbyId the lobbyId to get the tied players from
	 * @return the tied players from a lobby
	 */
	@GetMapping("/{lobbyId}/tied-players")
	public ResponseEntity<TiedPlayersResponse> getTiedPlayers(@PathVariable int lobbyId) {
		return ResponseEntity.ok(voteService.getTiedPlayers(lobbyId));

	}
}
