package com.captchatheai.backend.player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyRepository;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.exception.PlayerNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {
	
	
	private final LobbyService lobbyService;

	
	private final SimpMessagingTemplate messagingTemplate;
	public Player getPlayerById(String lobbyId, UUID playerId) {
		Player player = lobbyService.getLobbyById(lobbyId).getPlayersById().get(playerId);
		if (player == null) {
			throw new PlayerNotFoundException();
		}
		return player;

	}
	
	
	
	
	
	public UUID getPlayerIdBySessionId(String lobbyId, String sessionId) {
		UUID playerId = lobbyService.getLobbyById(lobbyId).getPlayerIdsBySessionId().get(sessionId);
		if (playerId == null) {
			throw new PlayerNotFoundException();
		}
		return playerId;
	}
	
	public PlayersDto getPlayers(String lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		
		Map<UUID, Player> playersById = lobby.getPlayersById();
		
		List<PlayerDto> players = playersById.values().stream().map((player) -> {
			return new PlayerDto(player.getName(), player.getAvatar(), 
					playerId.equals(lobby.getQuestionWriterId()), player.getId().equals(playerId));
			
			
		}).toList();
		
		return new PlayersDto(players);
			
			
		
	}
	
	
	public void broadcastPlayers(String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();
			
			playerIdsBySessionId.entrySet().stream().forEach((entry) -> 
				
				
			messagingTemplate.convertAndSend("/queue/lobby/" + lobbyId + "/players/" + entry.getKey(), getPlayers(lobbyId, entry.getValue())));
		
		}
		
	}
	
	public Player joinPlayer(String lobbyId, String sessionId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			String playerName;
			PlayerAvatar playerAvatar;
			PlayerState playerState;
			if (lobby.getPhase() == LobbyPhase.INTERMISSION || lobby.getPhase() == LobbyPhase.STARTING) {
				playerName = "WAITING";
				playerAvatar = PlayerAvatar.WAITING;
				playerState = PlayerState.WAITING;
			} else {
				playerName = "SPECTATOR";
				playerAvatar = PlayerAvatar.SPECTATING;
				playerState = PlayerState.SPECTATOR;
			}
			
			
			Player player = new Player(playerName, playerAvatar, playerState, null);
			
			List<UUID> players = lobby.getPlayerIds();
			Map<UUID, Player> playersById = lobby.getPlayersById();
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();
			players.add(player.getId());
			playersById.put(player.getId(), player);
			
			// the ai player will have a null sessionid, so dont add it to the map.
			if (sessionId != null) {
				playerIdsBySessionId.put(sessionId, player.getId());
				
			}
			
			broadcastPlayers(lobbyId);
			
			
			return player;
		}
	}
	
	public void disconnectPlayer(String lobbyId, UUID playerId) {
		
		
		
		
	}
	
	
	
}
