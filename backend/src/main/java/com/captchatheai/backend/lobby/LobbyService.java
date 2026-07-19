package com.captchatheai.backend.lobby;



import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LobbyService {

	
	
	
	/** The lobby repository to use */
	private final LobbyRepository lobbyRepository;
	
	
	
	
	public Lobby getLobbyById(String id) {
		return lobbyRepository.findById(id).orElseThrow();
	}
	
	
	
}
