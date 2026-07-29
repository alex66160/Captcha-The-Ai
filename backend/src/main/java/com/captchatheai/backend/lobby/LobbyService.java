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
import com.captchatheai.backend.question.QuestionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LobbyService {

	
	
	
	/** The lobby repository to use */
	private final LobbyRepository lobbyRepository;
	
	
	private final PlayerService playerService;
	
	private final QuestionService questionService;
	private final SimpMessagingTemplate messagingTemplate;
	

	private final static int MAX_PLAYERS = 8;
	
	
	
	
	
	public Lobby getLobbyById(int id) {
		return lobbyRepository.findById(id).orElseThrow(() -> new LobbyNotFoundException(id));
	}
	
	
	public LobbyInfoDto getLobbyInfo(int id) {
		Lobby lobby = getLobbyById(id);
		
		return new LobbyInfoDto(lobby.getPhase(), lobby.getPhaseEndTime(), lobby.getRoundCount());
		
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
	
	
	public void leaveLobby(int id, UUID playerId) {
		Lobby lobby = getLobbyById(id);
		synchronized(lobby) {
			playerService.removePlayer(id, playerId);
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
	

	
	public void deleteLobby(int id) {
		lobbyRepository.deleteById(id);
		
	}
   
	
	public void advancePhase(int id) {
		Lobby lobby = getLobbyById(id);
		synchronized(lobby) {
			switch(lobby.getPhase()) {
				
				case INTERMISSION -> {
					
					lobby.getPlayerIds().stream().filter((playerId) -> !playerId.equals(lobby.getAiPlayerId()))
					.forEach((playerId) -> playerService.kickPlayer(lobby.getId(), playerId));
					
					deleteLobby(lobby.getId());
					
				}
				
				case STARTING -> {
					lobby.setGameStartTime(Instant.now());
					playerService.assignPlayerIdentities(lobby.getId());
					lobby.setPhase(LobbyPhase.INTRO);
					lobby.setPhaseEndTime(Instant.now().plusSeconds(10));
					messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getId() + "/info", getLobbyInfo(lobby.getId()));
					
					
				}
				
				case INTRO -> {
					questionService.setNextQuestionWriter(lobby.getId());
					playerService.broadcastPlayers(id);
					lobby.setPhase(LobbyPhase.QUESTION_START);
					lobby.setPhaseEndTime(Instant.now().plusSeconds(5));
					messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getId() + "/info", getLobbyInfo(lobby.getId()));
					
					
					
				}
				
				case QUESTION_START -> {
					lobby.setPhase(LobbyPhase.QUESTION);
					lobby.setPhaseEndTime(Instant.now().plusSeconds(30));
					messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getId() + "/info", getLobbyInfo(lobby.getId()));
					if (lobby.getQuestionWriterId().equals(lobby.getAiPlayerId())) {
						// call llmservice to generate question
						
						
					}
					
				}
				
				case QUESTION -> {
					if (lobby.getQuestion() == null && lobby.getQuestionWriterId().equals(lobby.getAiPlayerId())) {
						lobby.setPhase(LobbyPhase.WIN);
						lobby.setPhaseEndTime(Instant.now().plusSeconds(10));
						messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getId() + "/info", getLobbyInfo(lobby.getId()));
						
						
					} else if (lobby.getQuestion() == null && !lobby.getQuestionWriterId().equals(lobby.getAiPlayerId())) {
						lobby.setEliminatedPlayerId(lobby.getQuestionWriterId());
						questionService.setNextQuestionWriter(id);
						playerService.kickPlayer(id, lobby.getEliminatedPlayerId());
						lobby.setPhase(LobbyPhase.QUESTION_EMPTY);
						lobby.setPhaseEndTime(Instant.now().plusSeconds(5));
						messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getId() + "/info", getLobbyInfo(lobby.getId()));
					} else {
					
						lobby.setPhase(LobbyPhase.ANSWER_START);
						lobby.setPhaseEndTime(Instant.now().plusSeconds(5));
						messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getId() + "/info", getLobbyInfo(lobby.getId()));
					}
					
				}
				
				case QUESTION_DISCONNECT -> {
					lobby.setPhase(LobbyPhase.QUESTION);
					lobby.setPhaseEndTime(Instant.now().plusSeconds(30));
					messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getId() + "/info", getLobbyInfo(lobby.getId()));
					
				}
				
				case QUESTION_EMPTY -> {
					lobby.setPhase(LobbyPhase.QUESTION);
					lobby.setPhaseEndTime(Instant.now().plusSeconds(30));
					messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getId() + "/info", getLobbyInfo(lobby.getId()));
					
				}
				
				case ANSWER_START -> System.out.println();
				
				case ANSWER -> System.out.println();
				
				case DISCUSS_START -> System.out.println();
				
				case DISCUSS -> System.out.println();
				
				case VOTING -> System.out.println();
				
				case VOTING_RESTART -> System.out.println();
				
				case REVEAL_START -> System.out.println();
				
				case REVEAL -> System.out.println();
				
				case REVEAL_TIE -> System.out.println();
				
				case REVEAL_END -> System.out.println();
				
				case WIN -> System.out.println();
				
				case LOSE -> System.out.println();
				
				case LOBBY_SHUTDOWN -> System.out.println();
				
			}
		
		
		}
	}
 
}
