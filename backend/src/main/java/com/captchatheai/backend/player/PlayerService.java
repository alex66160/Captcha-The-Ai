package com.captchatheai.backend.player;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyInfoDto;
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
	
	private final Map<String, Integer> lobbyIdByPlayerSessionId = new HashMap<>();
	private final LobbyService lobbyService;

	
	private final SimpMessagingTemplate messagingTemplate;
	public Player getPlayerById(int lobbyId, UUID playerId) {
		Player player = lobbyService.getLobbyById(lobbyId).getPlayersById().get(playerId);
		if (player == null) {
			throw new PlayerNotFoundException();
		}
		return player;

	}
	
	public UUID getPlayerIdByName(int lobbyId, String playerName) {
		UUID playerId = lobbyService.getLobbyById(lobbyId).getPlayerIdsByName().get(playerName);
		if (playerId == null) {
			throw new PlayerNotFoundException();
		}
		return playerId;
	}
	
	
	
	public UUID getPlayerIdBySessionId(int lobbyId, String playerSessionId) {
		UUID playerId = lobbyService.getLobbyById(lobbyId).getPlayerIdsBySessionId().get(playerSessionId);
		if (playerId == null) {
			throw new PlayerNotFoundException();
		}
		return playerId;
	}
	
	public PlayersDto getPlayers(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		
		List<UUID> playerIds = lobby.getPlayerIds();
		
		List<PlayerDto> players = playerIds.stream().map((playerIdFromPlayerIds) -> {
			Player player = getPlayerById(lobbyId, playerIdFromPlayerIds);
			return new PlayerDto(player.getName(), player.getAvatar(), 
					playerId.equals(lobby.getQuestionWriterId()), player.getId().equals(playerId));
			
			
		}).toList();
		
		return new PlayersDto(players);
			
			
		
	}
	
	
	public void broadcastPlayers(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();
			
			playerIdsBySessionId.entrySet().stream().forEach((entry) -> 
				
				
			messagingTemplate.convertAndSend("/queue/lobby/" + lobbyId + "/players/" + entry.getKey(), getPlayers(lobbyId, entry.getValue())));
		
		}
		
	}
	
	public Player addPlayer(int lobbyId, String sessionId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			String playerName;
			PlayerAvatar playerAvatar;
			PlayerState playerState;
			if (lobby.getPhase() == LobbyPhase.INTERMISSION || lobby.getPhase() == LobbyPhase.STARTING) {
				playerName = PlayerAvatar.HIDDEN.getName();
				playerAvatar = PlayerAvatar.HIDDEN;
				playerState = PlayerState.HIDDEN;
			} else {
				playerName = PlayerAvatar.SPECTATOR.getName();
				playerAvatar = PlayerAvatar.SPECTATOR;
				playerState = PlayerState.SPECTATOR;
			}
			
			
			Player player = new Player(sessionId, playerName, playerAvatar, playerState, null);
			
			List<UUID> players = lobby.getPlayerIds();
			Map<UUID, Player> playersById = lobby.getPlayersById();
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();
			players.add(player.getId());
			playersById.put(player.getId(), player);
			lobbyIdByPlayerSessionId.put(sessionId, lobbyId);
			
			// the ai player will have a null sessionid, so dont add it to the map.
			if (sessionId != null) {
				playerIdsBySessionId.put(sessionId, player.getId());
				
			}
			
			broadcastPlayers(lobbyId);
			
			if (lobby.getPhase() == LobbyPhase.INTERMISSION && players.size() == 3) {
				lobby.setPhase(LobbyPhase.STARTING);
				lobby.setPhaseEndTime(Instant.now().plusSeconds(30));
				// broadcast that we are in the starting phase.
				//messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/phase", new LobbyInfoDto(lobby.getPhase()));
				
			}
			
			return player;
		}
	}
	
	public Player removePlayer(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Player player = getPlayerById(lobbyId, playerId);
			PlayerState playerState = player.getState();
			
			if (playerState == PlayerState.DISCONNECTED) {
				throw new PlayerDisconnectedException();
			}
			List<UUID> playerIds = lobby.getPlayerIds();
			Map<UUID, Player> playersById = lobby.getPlayersById();
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();
			
			playerIds.remove(playerId);
			lobbyIdByPlayerSessionId.remove(player.getSessionId());
			if (playerState == PlayerState.HIDDEN || playerState == PlayerState.SPECTATOR) {
				playersById.remove(playerId);
				playerIdsBySessionId.remove(player.getSessionId());
			}
			
			if (playerState == PlayerState.ALIVE) {
				player.setState(PlayerState.DISCONNECTED);
			}
			
			
			
			broadcastPlayers(lobbyId);
			
			if (lobby.getPhase() == LobbyPhase.STARTING && playerIds.size() == 2) {
				lobby.setPhase(LobbyPhase.INTERMISSION);
				lobby.setPhaseEndTime(Instant.now());
			}
			return player;
		}
		
		
	}
	
	public void kickPlayer(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Player player = removePlayer(lobbyId, playerId);
			messagingTemplate.convertAndSend("/queue/lobby/" + lobbyId + "/disconnect/" + player.getSessionId());
		}
		
	}
	
	public void assignPlayerIdentities (int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			List<UUID> playerIds = lobby.getPlayerIds();
			
			
			Collections.shuffle(playerIds);
			
			List<PlayerAvatar> validAvatars = new ArrayList<>(Arrays.stream(PlayerAvatar.values())
					.filter((playerAvatar) -> playerAvatar != PlayerAvatar.HIDDEN && playerAvatar != PlayerAvatar.SPECTATOR).toList());
			
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
	
	public void clearPlayerIdentities(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {

			
			List<UUID> playerIds = lobby.getPlayerIds();

			
			for (UUID playerId : playerIds) {
				Player player = getPlayerById(lobbyId, playerId);
				player.setName(PlayerAvatar.HIDDEN.getName());
				player.setAvatar(PlayerAvatar.HIDDEN);
				player.setState(PlayerState.HIDDEN);
						
			}
			
			Collections.shuffle(playerIds);
			broadcastPlayers(lobbyId);
			
			
			
		}
	}
	
	public void removeDisconnectedPlayers(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {

			
			Map<UUID, Player> playersById = lobby.getPlayersById();
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();

			List<String> disconnectedSessionIds = playerIdsBySessionId.keySet().stream().filter((sessionId) -> {
				
				Player player = playersById.get(playerIdsBySessionId.get(sessionId));
				return player.getState() == PlayerState.DISCONNECTED;
				
				}).toList();
			
	
			disconnectedSessionIds.stream().forEach((sessionId) -> {
				
				UUID removedPlayerId = playerIdsBySessionId.remove(sessionId);
				playersById.remove(removedPlayerId);
				
			});
			
			
		}
	}
	
	public EliminatedPlayerDto getEliminatedPlayer(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Player eliminatedPlayer = getPlayerById(lobbyId, lobby.getEliminatedPlayerId());
			return new EliminatedPlayerDto(eliminatedPlayer.getName(), 
					eliminatedPlayer.getAvatar(), eliminatedPlayer.getId().equals(lobby.getAiPlayerId()));
			
			
			
			
		}
	}
	
	public AiPlayerDto getAiPlayer(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			if (lobby.getPhase() != LobbyPhase.AI_PLAYER_WON && lobby.getPhase() != LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND && lobby.getPhase() != LobbyPhase.HUMAN_PLAYERS_WON && lobby.getPhase() != LobbyPhase.NOT_ENOUGH_PLAYERS) {
				throw new AiPlayerAccessDeniedException();
			}
			
			Player aiPlayer = getPlayerById(lobbyId, lobby.getAiPlayerId());
			return new AiPlayerDto(aiPlayer.getName(), aiPlayer.getAvatar());
		}
	}
	
	
	
}
