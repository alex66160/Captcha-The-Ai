package com.captchatheai.backend.lobby;



import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.exception.IncorrectLobbyPasswordException;
import com.captchatheai.backend.lobby.exception.LobbyFullException;
import com.captchatheai.backend.lobby.exception.LobbyNotFoundException;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.exception.PlayerNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LobbyService {

	
	
	
	/** The lobby repository to use */
	private final LobbyRepository lobbyRepository;
	
	
	private final PlayerService playerService;
	
	private final SimpMessagingTemplate messagingTemplate;
	

	private final static int MAX_PLAYERS = 8;
	
	
	
	
	
	public Lobby getLobbyById(int id) {
		return lobbyRepository.findById(id).orElseThrow(() -> new LobbyNotFoundException(id));
	}
	
	public void joinLobby(String sessionId) {
		while (true) {
			// first check if a lobby exists that isnt full and is a public lobby
			List<Lobby> lobbiesToJoin = lobbyRepository.findAll().stream().filter((lobby) -> lobby.getPlayerIds().size() <= MAX_PLAYERS && lobby.getPassword() == null).toList();
			Lobby lobbyToJoin;
			// if no avaiable lobbies exist, just make a new one.
			if (lobbiesToJoin.isEmpty()) {
				createLobby(sessionId, null);
				break;
				
				
			} else {
				// set lobbytojoin from a random lobby that isnt full as a backup in case next filter returns empty
				lobbyToJoin = lobbiesToJoin.get(ThreadLocalRandom.current().nextInt(lobbiesToJoin.size()));
				// attempt to filter further so that players can join games quicker.
				lobbiesToJoin = lobbiesToJoin.stream().filter((lobby) -> lobby.getPhase() == LobbyPhase.INTERMISSION || lobby.getPhase() == LobbyPhase.STARTING).toList();
				
				// if a lobby exists in intermission or start, set it equal to a lobby there.
				if (!lobbiesToJoin.isEmpty()) {
					lobbyToJoin = lobbiesToJoin.get(ThreadLocalRandom.current().nextInt(lobbiesToJoin.size()));
	
				} 
				
			}
			
			synchronized(lobbyToJoin) {
				if (lobbyToJoin.getPlayerIds().size() >= MAX_PLAYERS) {
					// retry if the max players is violated by the time we tried to sync.
					continue;
				}
				
				playerService.addPlayer(lobbyToJoin.getId(), sessionId);
				messagingTemplate.convertAndSend("/queue/join/" + sessionId, new LobbyIdDto(lobbyToJoin.getId()));
				break;
				
			}
		}
		
		
		
	
		
	}
	
	
	public void joinLobbyById(int id, String sessionId, String password) {
		
		
		Lobby lobby = getLobbyById(id);
		synchronized(lobby) {
			
			if (lobby.getPassword() != null && !lobby.getPassword().equals(password)) {
				throw new IncorrectLobbyPasswordException();
			}
			
			
			
			if (lobby.getPlayerIds().size() >= MAX_PLAYERS) {
				throw new LobbyFullException();
			}
			
			
			playerService.addPlayer(lobby.getId(), sessionId);
			messagingTemplate.convertAndSend("/queue/join/" + sessionId, new LobbyIdDto(lobby.getId()));
			
			
		}
		
	}
	public void createLobby(String sessionId, String password) {
		while (true) {
			int lobbyId = ThreadLocalRandom.current().nextInt(100000, 1000000);
			Lobby lobby = new Lobby(lobbyId, password);
			synchronized(lobby) {
				if (lobbyRepository.create(lobby)) {
					Player aiPlayer = playerService.addPlayer(lobbyId, null);
					lobby.setAiPlayerId(aiPlayer.getId());
					playerService.addPlayer(lobbyId, sessionId);
					messagingTemplate.convertAndSend("/queue/join/" + sessionId, new LobbyIdDto(lobby.getId()));
					break;
				}
			}
		}
	
	}
	
	// implement leave lobby
	
	public void deleteLobby(int id) {
		lobbyRepository.deleteById(id);
		
	}
   
    
 
}
