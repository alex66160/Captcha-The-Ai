package com.captchatheai.backend.lobby;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.exception.LobbyNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * The LobbyLookup class allows the lookup of a lobby by its id.
 * 
 * @author Alex Liu
 */
@Service
@RequiredArgsConstructor
public class LobbyLookup {

	private final LobbyRepository lobbyRepository;

	/**
	 * The getLobbyById method returns a lobby by its id. This method was originally
	 * from LobbyService, but was extracted to LobbyLookup to avoid circular
	 * dependencies.
	 * 
	 * @param lobbyId the lobby to find
	 * @return the lobby that was found
	 * @throws LobbyNotFoundException if the lobby was not found
	 */
	public Lobby getLobbyById(int lobbyId) {
		return lobbyRepository.findById(lobbyId)
				.orElseThrow(() -> new LobbyNotFoundException("Lobby Id: " + lobbyId + ", Lobby was not found."));
	}

}
