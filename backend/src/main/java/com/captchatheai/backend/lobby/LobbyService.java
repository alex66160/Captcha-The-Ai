package com.captchatheai.backend.lobby;



import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LobbyService {

	
	
	
	/** The lobby repository to use */
	private final LobbyRepository lobbyRepository;
	
	
	
	
	public Lobby getLobbyById(String id) {
		return lobbyRepository.findById(id).orElseThrow(() -> new LobbyNotFoundException(id));
	}
	
	
    public LobbyPhase getLobbyPhase(String lobbyId) {
    	return getLobbyById(lobbyId).getLobbyPhase();
    }
    
    public Player getPlayerById(String lobbyId, UUID playerId) {
    	Player player = getLobbyById(lobbyId).getPlayersById().get(playerId);
    	if (player == null) {
    		throw new PlayerNotFoundException();
    	}
    	return player;
    	
    }
}
