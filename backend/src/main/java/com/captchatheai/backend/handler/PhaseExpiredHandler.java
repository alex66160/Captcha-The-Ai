package com.captchatheai.backend.handler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.captchatheai.backend.ai.AiEvent;
import com.captchatheai.backend.ai.ScheduledAiEvent;
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
			if (lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobby.setScheduledAiEvent(new ScheduledAiEvent(AiEvent.GENERATE_QUESTION, Instant.now().plusSeconds(LobbyService.QUESTION_DURATION / 2)));
			}
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION);
		}
	}
	
	public void handleQuestionExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
		
			if (lobby.getQuestion() == null && lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND);
				return;
			} 
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
			if (!lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobby.setScheduledAiEvent(new ScheduledAiEvent(AiEvent.GENERATE_ANSWER, Instant.now().plusSeconds(LobbyService.ANSWER_DURATION / 2)));
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
			
			if (!lobby.getAnswersById().containsKey(lobby.getAiPlayerId()) && !lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND);
				return;
			} 
			
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.DISCUSS_START);
			
		}
	}
	
	public void handleDiscussStartExpired(int lobbyId) {
		
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.DISCUSS);
		}
	}
	
	public void handleDiscussExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			
			lobby.setScheduledAiEvent(new ScheduledAiEvent(AiEvent.GENERATE_VOTE, Instant.now().plusSeconds(LobbyService.VOTING_DURATION / 2)));
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.VOTING);
		}
	}
	
	public void handleVotingExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			Map<UUID, UUID> voteTargetByVoter = lobby.getVoteTargetByVoter();
			List<UUID> playerIdsToKick = lobby.getPlayerIds().stream().filter((playerId) -> !voteTargetByVoter.containsKey(playerId) && 
					playerService.getPlayerById(lobbyId, playerId).getState() == PlayerState.ALIVE && !playerId.equals(lobby.getAiPlayerId())).toList();
			playerIdsToKick.stream().forEach((playerId) -> playerService.kickPlayer(lobbyId, playerId));
			
			if (!voteTargetByVoter.containsKey(lobby.getAiPlayerId())) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND);
				return;
			}
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL_START);
		}
	}
	
	public void handleVotingRestartExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			voteService.clearVotes(lobbyId);
			lobby.setScheduledAiEvent(new ScheduledAiEvent(AiEvent.GENERATE_VOTE, Instant.now().plusSeconds(LobbyService.VOTING_DURATION / 2)));
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.VOTING);
		}
	}
	public void handleRevealStartExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
		
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL);
		
		}
	}
	
	public void handleRevealExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			
	
			
			voteService.calculateVotes(lobbyId);
			if (lobby.getTiedPlayerIds().size() == 1) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL_END);
			} else {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL_TIE);
			}
			
			
		
		}
	}
	
	public void handleRevealTieExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			
	
			
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL_END);
			
			
		
		}
	}
	
	public void handleRevealEndExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			playerService.setPlayerAsSpectator(lobbyId, lobby.getEliminatedPlayerId());
			if (lobby.getEliminatedPlayerId().equals(lobby.getAiPlayerId())) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.HUMAN_PLAYERS_WON);
				return;
			}
			
			if (lobby.getAlivePlayerCount() <= 2) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_WON);
				return;
			}
			
			questionService.setNextQuestionWriter(lobbyId);
			lobbyService.prepareLobbyForNextRound(lobbyId);
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION_START);
			
			
		
		}
	}
	
	public void handleGameResultExpired(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			
		
			lobbyService.prepareLobbyForNextGame(lobbyId);
			if (lobby.getPlayerIds().size() <= 2) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.INTERMISSION);
			} else {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.STARTING);
			}
			
			
		
		}
	}
	
}
