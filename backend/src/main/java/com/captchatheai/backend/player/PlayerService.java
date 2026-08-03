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
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyInfoDto;
import com.captchatheai.backend.lobby.LobbyPhase;

import com.captchatheai.backend.lobby.LobbyRepository;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.exception.AiPlayerAccessDeniedException;
import com.captchatheai.backend.player.exception.PlayerDisconnectedException;
import com.captchatheai.backend.player.exception.PlayerNotFoundException;
import com.captchatheai.backend.question.QuestionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {
	
	private final Map<String, Integer> lobbyIdByPlayerSessionId = new ConcurrentHashMap<>();
	private final LobbyService lobbyService;

	private final QuestionService questionService;
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
		
		List<UUID> playerIds = lobby.getPlayerIds().stream().filter((playerIdFromLobby) -> getPlayerById(lobbyId, playerIdFromLobby).getState() != PlayerState.DISCONNECTED).toList();
		
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
			
			if (lobby.getPhase() == LobbyPhase.INTERMISSION && lobby.getPlayerCount() == 3) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.STARTING);
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
			
			lobbyIdByPlayerSessionId.remove(player.getSessionId());
		
			
			if (playerState == PlayerState.HIDDEN || playerState == PlayerState.SPECTATOR) {
				playerIds.remove(playerId);
				playersById.remove(playerId);
				playerIdsBySessionId.remove(player.getSessionId());
			}
			
			if (playerState == PlayerState.ALIVE) {
				player.setState(PlayerState.DISCONNECTED);
			}
			
			
			
			broadcastPlayers(lobbyId);
			
			
			if (lobby.getPhase() != LobbyPhase.INTERMISSION && lobby.getPhase() != LobbyPhase.STARTING && lobby.getPhase() != LobbyPhase.REVEAL_END && 
					lobby.getAlivePlayerCount() == 2) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.NOT_ENOUGH_PLAYERS);
			}
			
			if (lobby.getPhase() == LobbyPhase.QUESTION && playerId.equals(lobby.getQuestionWriterId())) {
				lobby.setEliminatedPlayerId(playerId);
				questionService.setNextQuestionWriter(lobbyId);
				
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION_DISCONNECT);
			}
			
			if (lobby.getPhase() == LobbyPhase.STARTING && playerIds.size() == 2) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.INTERMISSION);
			}
			
			if (lobby.getPhase() == LobbyPhase.VOTING && player.getState() == PlayerState.ALIVE) {
				lobby.setEliminatedPlayerId(playerId);
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.VOTING_RESTART);

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
	
	public void setPlayerAsSpectator(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Player player = getPlayerById(lobbyId, playerId);
			player.setAvatar(PlayerAvatar.SPECTATOR);
			player.setName(PlayerAvatar.SPECTATOR.getName());
			player.setState(PlayerState.SPECTATOR);
			broadcastPlayers(lobbyId);
		}
	}
	
	
	public void assignPlayerIdentities (int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			List<UUID> playerIds = lobby.getPlayersById().entrySet().stream().filter((entry) -> entry.getValue().getState() != PlayerState.DISCONNECTED).map((entry) -> entry.getKey()).toList();
			
			
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

			
			List<UUID> playerIds = lobby.getPlayersById().entrySet().stream().filter((entry) -> entry.getValue().getState() != PlayerState.DISCONNECTED).map((entry) -> entry.getKey()).toList();

			lobby.getPlayerIdsByName().clear();
			for (UUID playerId : playerIds) {
				Player player = getPlayerById(lobbyId, playerId);
				player.setName(PlayerAvatar.HIDDEN.getName());
				player.setAvatar(PlayerAvatar.HIDDEN);
				player.setState(PlayerState.HIDDEN);
						
			}
			
			broadcastPlayers(lobbyId);
			
			
			
		}
	}
	
	public void removeDisconnectedPlayers(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {

			List<UUID> playerIds = lobby.getPlayerIds();
			Map<UUID, Player> playersById = lobby.getPlayersById();
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();

			List<String> disconnectedSessionIds = playerIdsBySessionId.keySet().stream().filter((sessionId) -> {
				
				Player player = playersById.get(playerIdsBySessionId.get(sessionId));
				return player.getState() == PlayerState.DISCONNECTED;
				
				}).toList();
			
	
			disconnectedSessionIds.stream().forEach((sessionId) -> {
				
				UUID removedPlayerId = playerIdsBySessionId.remove(sessionId);
				playerIds.remove(removedPlayerId);
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
	
	@EventListener
	public void playerDisconnectListener(SessionDisconnectEvent sessionDisconnectEvent) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(sessionDisconnectEvent.getMessage());
		String sessionId = accessor.getSessionId();
		
		
		
		if (lobbyIdByPlayerSessionId.containsKey(sessionId)) {
			Lobby lobby = lobbyService.getLobbyById(lobbyIdByPlayerSessionId.get(sessionId));
			synchronized(lobby) {

				if (lobby.getPlayerIdsBySessionId().containsKey(sessionId)) {
					removePlayer(lobby.getId(), getPlayerIdBySessionId(lobby.getId(), sessionId));
				}
			}
		}
	}
	
	
	
}
