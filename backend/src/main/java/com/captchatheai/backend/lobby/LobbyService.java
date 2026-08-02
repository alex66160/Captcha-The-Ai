package com.captchatheai.backend.lobby;



import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.answer.AnswerService;
import com.captchatheai.backend.handler.PhaseExpiredHandler;
import com.captchatheai.backend.lobby.exception.IncorrectLobbyPasswordException;
import com.captchatheai.backend.lobby.exception.LobbyFullException;
import com.captchatheai.backend.lobby.exception.LobbyNotFoundException;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.exception.PlayerNotFoundException;
import com.captchatheai.backend.question.QuestionService;
import com.captchatheai.backend.vote.VoteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LobbyService {

	
	
	
	/** The lobby repository to use */
	private final LobbyRepository lobbyRepository;
	
	private final PhaseExpiredHandler phaseExpiredHandler;
	private final PlayerService playerService;
	
	private final QuestionService questionService;
	private final AnswerService answerService;
	private final VoteService voteService;
	private final SimpMessagingTemplate messagingTemplate;
	

	private final static int MAX_PLAYERS = 8;
	
	public final static int INTERMISSION_DURATION = 600;
	
	public final static int STARTING_DURATION = 30;
	
	public final static int INTRO_DURATION = 10;
	
	public final static int QUESTION_START_DURATION = 3;
	
	public final static int QUESTION_DURATION = 30;
	
	public final static int QUESTION_DISCONNECT_DURATION = 30;
	
	public final static int QUESTION_EMPTY_DURATION = 30;
	
	public final static int ANSWER_START_DURATION = 30;
	
	public final static int ANSWER_DURATION = 30;
	
	public final static int DISCUSS_START_DURATION = 30;
	
	public final static int DISCUSS_DURATION = 30;
	
	public final static int VOTING_DURATION = 30;
	
	public final static int VOTING_RESTART_DURATION = 30;
	
	public final static int REVEAL_START_DURATION = 30;
	
	public final static int REVEAL_DURATION = 30;
	
	public final static int REVEAL_TIE_DURATION = 30;
	
	public final static int REVEAL_END_DURATION = 30;
	
	public final static int GAME_RESULT_DURATION = 10;
	
	
	
	
	
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
			List<Lobby> lobbiesToJoin = lobbyRepository.findAll().stream().filter((lobby) -> lobby.getPlayerCount() <= MAX_PLAYERS && lobby.getPassword() == null).toList();
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
				if (lobbyToJoin.getPlayerCount() >= MAX_PLAYERS) {
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
			
			
			
			if (lobby.getPlayerCount() >= MAX_PLAYERS) {
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
			Lobby lobby = new Lobby(password);
			synchronized(lobby) {
				if (lobbyRepository.create(lobby)) {
					
					transitionToPhase(lobby.getId(), LobbyPhase.INTERMISSION);
					
					Player aiPlayer = playerService.addPlayer(lobby.getId(), null);
					lobby.setAiPlayerId(aiPlayer.getId());
					playerService.addPlayer(lobby.getId(), sessionId);
					messagingTemplate.convertAndSend("/queue/join/" + sessionId, new LobbyIdDto(lobby.getId()));
					break;
				}
			}
		}
	
	}
	

	
	public void deleteLobby(int id) {
		Lobby lobby = getLobbyById(id);
		synchronized(lobby) {
			lobbyRepository.deleteById(id);
		}
		
	}
   
	
	public void prepareLobbyForNextGame(int id) {
		Lobby lobby = getLobbyById(id);
		synchronized(lobby) {
			voteService.clearVotes(id);
			questionService.deleteQuestion(id);
			answerService.deleteAnswers(id);
			playerService.clearPlayerIdentities(id);
			playerService.removeDisconnectedPlayers(id);
			lobby.setQuestionWriterId(null);
			lobby.setEliminatedPlayerId(null);
			lobby.getChatHistory().clear();
			lobby.getGameHistory().clear();
			lobby.setGameStartTime(null);
			lobby.setRoundCount(1);
		}
	}
	
	public void prepareLobbyForNextRound(int id) {
		
		Lobby lobby = getLobbyById(id);
		synchronized(lobby) {
			voteService.clearVotes(id);
			questionService.deleteQuestion(id);
			answerService.deleteAnswers(id);
			
			lobby.setRoundCount(lobby.getRoundCount() + 1);
		}
		

	}
	
	
	public void advancePhase(int id) {
		Lobby lobby = getLobbyById(id);
		synchronized(lobby) {
			switch(lobby.getPhase()) {
				
				case INTERMISSION -> phaseExpiredHandler.handleIntermissionExpired(id);
				
				case STARTING -> phaseExpiredHandler.handleStartingExpired(id);
				
				case INTRO -> phaseExpiredHandler.handleIntroExpired(id);
				
				case QUESTION_START -> phaseExpiredHandler.handleQuestionStartExpired(id);
				
				case QUESTION -> phaseExpiredHandler.handleQuestionExpired(id);
				
				case QUESTION_DISCONNECT -> phaseExpiredHandler.handleQuestionDisconnectExpired(id);
				
				case QUESTION_EMPTY -> phaseExpiredHandler.handleQuestionEmptyExpired(id);
				
				case ANSWER_START -> phaseExpiredHandler.handleAnswerStartExpired(id);
				
				case ANSWER -> phaseExpiredHandler.handleAnswerExpired(id);
				
				case DISCUSS_START -> phaseExpiredHandler.handleDiscussStartExpired(id);
				
				case DISCUSS -> phaseExpiredHandler.handleDiscussExpired(id);
				
				case VOTING -> phaseExpiredHandler.handleVotingExpired(id);
				
				case VOTING_RESTART -> phaseExpiredHandler.handleVotingRestartExpired(id);
				
				case REVEAL_START -> phaseExpiredHandler.handleRevealStartExpired(id);
				
				case REVEAL -> phaseExpiredHandler.handleRevealExpired(id);
				
				case REVEAL_TIE -> phaseExpiredHandler.handleRevealTieExpired(id);
				
				case REVEAL_END -> phaseExpiredHandler.handleRevealEndExpired(id);
				
				case AI_PLAYER_WON -> phaseExpiredHandler.handleGameResultExpired(id);
				
				case AI_PLAYER_FAILED_TO_RESPOND -> phaseExpiredHandler.handleGameResultExpired(id);
				
				case HUMAN_PLAYERS_WON -> phaseExpiredHandler.handleGameResultExpired(id);
				
				case NOT_ENOUGH_PLAYERS -> phaseExpiredHandler.handleGameResultExpired(id);
				
			}
		
		
		}
	}
	
	public void transitionToPhase(int id, LobbyPhase phase) {
		Lobby lobby = getLobbyById(id);
		synchronized(lobby) {
			lobby.setPhase(phase);
			switch(phase) {
				case INTERMISSION -> lobby.setPhaseEndTime(Instant.now().plusSeconds(INTERMISSION_DURATION));
				
				case STARTING -> lobby.setPhaseEndTime(Instant.now().plusSeconds(STARTING_DURATION));
				
				case INTRO -> lobby.setPhaseEndTime(Instant.now().plusSeconds(INTRO_DURATION));
				
				case QUESTION_START -> lobby.setPhaseEndTime(Instant.now().plusSeconds(QUESTION_START_DURATION));
				
				case QUESTION -> lobby.setPhaseEndTime(Instant.now().plusSeconds(QUESTION_DURATION));
				
				case QUESTION_DISCONNECT -> lobby.setPhaseEndTime(Instant.now().plusSeconds(QUESTION_DISCONNECT_DURATION));
				
				case QUESTION_EMPTY -> lobby.setPhaseEndTime(Instant.now().plusSeconds(QUESTION_EMPTY_DURATION));
				
				case ANSWER_START -> lobby.setPhaseEndTime(Instant.now().plusSeconds(ANSWER_START_DURATION));
				
				case ANSWER -> lobby.setPhaseEndTime(Instant.now().plusSeconds(ANSWER_DURATION));
				
				case DISCUSS_START -> lobby.setPhaseEndTime(Instant.now().plusSeconds(DISCUSS_START_DURATION));
				
				case DISCUSS -> lobby.setPhaseEndTime(Instant.now().plusSeconds(DISCUSS_DURATION));
				
				case VOTING -> lobby.setPhaseEndTime(Instant.now().plusSeconds(VOTING_DURATION));
				
				case VOTING_RESTART -> lobby.setPhaseEndTime(Instant.now().plusSeconds(VOTING_RESTART_DURATION));
				
				case REVEAL_START -> lobby.setPhaseEndTime(Instant.now().plusSeconds(REVEAL_START_DURATION));
				
				case REVEAL -> lobby.setPhaseEndTime(Instant.now().plusSeconds(REVEAL_DURATION));
				
				case REVEAL_TIE -> lobby.setPhaseEndTime(Instant.now().plusSeconds(REVEAL_TIE_DURATION));
				
				case REVEAL_END -> lobby.setPhaseEndTime(Instant.now().plusSeconds(REVEAL_END_DURATION));
				
				case AI_PLAYER_WON, AI_PLAYER_FAILED_TO_RESPOND, HUMAN_PLAYERS_WON, NOT_ENOUGH_PLAYERS -> 
				lobby.setPhaseEndTime(Instant.now().plusSeconds(GAME_RESULT_DURATION));

			
			
			
			
			
			}
			messagingTemplate.convertAndSend("/topic/lobby/" + id + "/info", getLobbyInfo(id));			
		}
	}
 
}
