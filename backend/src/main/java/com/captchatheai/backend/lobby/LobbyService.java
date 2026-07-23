package com.captchatheai.backend.lobby;



import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.exception.LobbyNotFoundException;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.exception.PlayerNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LobbyService {

	
	
	
	/** The lobby repository to use */
	private final LobbyRepository lobbyRepository;
	
	
	
	
	public Lobby getLobbyById(String id) {
		return lobbyRepository.findById(id).orElseThrow(() -> new LobbyNotFoundException(id));
	}
	
	
   
    
 
}
