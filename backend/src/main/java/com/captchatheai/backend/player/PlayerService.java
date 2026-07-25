package com.captchatheai.backend.player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyRepository;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.exception.AiPlayerAccessDeniedException;
import com.captchatheai.backend.player.exception.PlayerDisconnectedException;
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
	
	public UUID getPlayerIdByName(String lobbyId, String playerName) {
		UUID playerId = lobbyService.getLobbyById(lobbyId).getPlayerIdsByName().get(playerName);
		if (playerId == null) {
			throw new PlayerNotFoundException();
		}
		return playerId;
	}
	
	
	
	public UUID getPlayerIdBySessionId(String lobbyId, String playerSessionId) {
		UUID playerId = lobbyService.getLobbyById(lobbyId).getPlayerIdsBySessionId().get(playerSessionId);
		if (playerId == null) {
			throw new PlayerNotFoundException();
		}
		return playerId;
	}
	
	public PlayersDto getPlayers(String lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		
		List<UUID> playerIds = lobby.getPlayerIds();
		
		List<PlayerDto> players = playerIds.stream().map((playerIdFromPlayerIds) -> {
			Player player = getPlayerById(lobbyId, playerIdFromPlayerIds);
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
				playerAvatar = PlayerAvatar.SPECTATOR;
				playerState = PlayerState.SPECTATOR;
			}
			
			
			Player player = new Player(sessionId, playerName, playerAvatar, playerState, null);
			
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
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Player player = getPlayerById(lobbyId, playerId);
			PlayerState playerState = player.getState();
			
			if (playerState == PlayerState.DISCONNECTED) {
				throw new PlayerDisconnectedException();
			}
			List<UUID> players = lobby.getPlayerIds();
			Map<UUID, Player> playersById = lobby.getPlayersById();
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();
			
			players.remove(playerId);
			
			if (playerState == PlayerState.WAITING || playerState == PlayerState.SPECTATOR) {
				playersById.remove(playerId);
				playerIdsBySessionId.remove(player.getSessionId());
			}
			
			if (playerState == PlayerState.ALIVE) {
				player.setState(PlayerState.DISCONNECTED);
			}
			
			broadcastPlayers(lobbyId);
			
			
			
		}
		
		
	}
	
	public void assignPlayerIdentites (String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			List<UUID> playerIds = lobby.getPlayerIds();
			List<PlayerAvatar> validAvatars = Arrays.stream(PlayerAvatar.values())
					.filter((playerAvatar) -> playerAvatar != PlayerAvatar.WAITING && playerAvatar != PlayerAvatar.SPECTATOR).toList();
			
			Collections.shuffle(validAvatars);
			Map<String, UUID> playerIdsByName = lobby.getPlayerIdsByName();
			
			for (int i = 0; i < playerIds.size(); i++) {
				
				Player player = getPlayerById(lobbyId, playerIds.get(i));
				player.setAvatar(validAvatars.get(i));
				player.setName(validAvatars.get(i).getName());
				player.setState(PlayerState.ALIVE);
				playerIdsByName.put(player.getName(), player.getId());
			}
			
			
			broadcastPlayers(lobbyId);
			
			
			
		}
	}
	
	public EliminatedPlayerDto getEliminatedPlayer(String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Player eliminatedPlayer = getPlayerById(lobbyId, lobby.getEliminatedPlayerId());
			return new EliminatedPlayerDto(eliminatedPlayer.getName(), 
					eliminatedPlayer.getAvatar(), eliminatedPlayer.getId().equals(lobby.getAiPlayerId()));
			
			
			
			
		}
	}
	
	public AiPlayerDto getAiPlayer(String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			if (lobby.getPhase() != LobbyPhase.WIN && lobby.getPhase() != LobbyPhase.LOSE) {
				throw new AiPlayerAccessDeniedException();
			}
			
			Player aiPlayer = getPlayerById(lobbyId, lobby.getAiPlayerId());
			return new AiPlayerDto(aiPlayer.getName(), aiPlayer.getAvatar());
		}
	}
	
}
