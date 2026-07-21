package com.captchatheai.backend.player;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.LobbyRepository;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.exception.PlayerNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {
	
	
	private final LobbyService lobbyService;

	public Player getPlayerById(String lobbyId, UUID playerId) {
		Player player = lobbyService.getLobbyById(lobbyId).getPlayersById().get(playerId);
		if (player == null) {
			throw new PlayerNotFoundException();
		}
		return player;

	}
	
	
	public Player getPlayerBySessionId(String lobbyId, String sessionId) {
		Player player = lobbyService.getLobbyById(lobbyId).getPlayersBySessionId().get(sessionId);
		if (player == null) {
			throw new PlayerNotFoundException();
		}
		return player;
	}
	
	
	
	
}
