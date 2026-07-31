package com.captchatheai.backend.handler;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.captchatheai.backend.answer.AnswerService;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerState;
import com.captchatheai.backend.question.QuestionService;
import com.captchatheai.backend.vote.VoteService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PhaseExpiredHandler {

	
	private final LobbyService lobbyService;
	private final PlayerService playerService;
	
	private final QuestionService questionService;
	
	private final AnswerService answerService;
	
	private final VoteService voteService;
	
	public void handleIntermissionExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			List<UUID> playerIdsToKick = lobby.getPlayerIds().stream().filter((playerId) -> !playerId.equals(lobby.getAiPlayerId())).toList();
			playerIdsToKick.stream().forEach((playerId) -> playerService.kickPlayer(lobbyId, playerId));
			
			lobbyService.deleteLobby(lobbyId);
			
			
		}
	}
	
	public void handleStartingExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			playerService.assignPlayerIdentities(lobbyId);
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.INTRO);
		}
	}
	
	public void handleIntroExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			questionService.setNextQuestionWriter(lobbyId);
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION_START);
		}
	}
	
	public void handleQuestionStartExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {

			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION);
		}
	}
	
	public void handleQuestionExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
		
			
			if (lobby.getQuestion() == null && !lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobby.setEliminatedPlayerId(lobby.getQuestionWriterId());
				questionService.setNextQuestionWriter(lobbyId);
				playerService.kickPlayer(lobbyId, lobby.getEliminatedPlayerId());
				
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION_EMPTY);
				return;
			} 
			
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.ANSWER_START);
			
		}
	}
	
	public void handleQuestionDisconnectExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			questionService.deleteQuestion(lobbyId);
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION);
			
		}
	}
	
	public void handleQuestionEmptyExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION);
			
		}
	}
	
	public void handleAnswerStartExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			if (lobby.getQuestion() == null && lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND);
				return;
			} 
	
			
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.ANSWER);
			
		}
	}
	
	public void handleAnswerExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Map<UUID, String> answersById = lobby.getAnswersById();
			List<UUID> playerIdsToKick = lobby.getPlayerIds().stream().filter((playerId) -> !answersById.containsKey(playerId) && 
					playerService.getPlayerById(lobbyId, playerId).getState() == PlayerState.ALIVE && !playerId.equals(lobby.getAiPlayerId()) && 
					!playerId.equals(lobby.getQuestionWriterId())).toList();
			playerIdsToKick.stream().forEach((playerId) -> playerService.kickPlayer(lobbyId, playerId));
			
	
			
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.DISCUSS_START);
			
		}
	}
	
	public void handleDiscussStartExpired(int lobbyId) {
		
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			if (!lobby.getAnswersById().containsKey(lobby.getAiPlayerId()) && !lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND);
				return;
			} 
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.DISCUSS);
		}
	}
	
	public void handleDiscussExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.VOTING);
		}
	}
	
	public void handleVotingExpired(int lobbyId) {
		
	}
	
	public void handleVotingRestartExpired(int lobbyId) {
		
	}
	public void handleRevealStartExpired(int lobbyId) {
		
	}
	
	public void handleRevealExpired(int lobbyId) {
		
	}
	
	public void handleRevealTieExpired(int lobbyId) {
		
	}
	
	public void handleRevealEndExpired(int lobbyId) {
		
	}
	
	public void handleGameResultExpired(int lobbyId) {
		
	}
	
}
