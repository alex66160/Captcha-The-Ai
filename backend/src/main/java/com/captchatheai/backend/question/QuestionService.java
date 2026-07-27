package com.captchatheai.backend.question;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyRepository;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerState;
import com.captchatheai.backend.question.exception.NotQuestionPhaseException;
import com.captchatheai.backend.question.exception.NotQuestionWriterException;
import com.captchatheai.backend.question.exception.QuestionAlreadyWrittenException;
import com.captchatheai.backend.question.exception.QuestionNotAvailableException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

	
	
	
	private final LobbyService lobbyService;
	
	private final PlayerService playerService;
	
	
	public QuestionDto getQuestion(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			Player questionWriter = playerService.getPlayerById(lobbyId, lobby.getQuestionWriterId());
			if (lobby.getPhase() != LobbyPhase.ANSWER && 
					lobby.getPhase() != LobbyPhase.DISCUSS && 
					lobby.getPhase() != LobbyPhase.VOTING) {
				
				throw new QuestionNotAvailableException();
				
			}
			
			
			return new QuestionDto(questionWriter.getName(), questionWriter.getAvatar(), lobby.getQuestion());
		}
		
		
	}
	
	public void chooseNextQuestionWriter(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			List<UUID> playersById = lobby.getPlayerIds();
			UUID oldQuestionWriterId = lobby.getQuestionWriterId();
			
		
		
			
			int idx = playersById.indexOf(oldQuestionWriterId);
			
			
			UUID newQuestionWriterId = playersById.get((++idx) % playersById.size());
			while (playerService.getPlayerById(lobbyId, newQuestionWriterId).getState() != PlayerState.ALIVE) {
				newQuestionWriterId = playersById.get((++idx) % playersById.size());
			}
			
			
			lobby.setQuestionWriterId(newQuestionWriterId);
			
			
		}
	}
	
	
	public void sendQuestion(int lobbyId, UUID playerId, String question) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			if (lobby.getPhase() != LobbyPhase.QUESTION) {
				throw new NotQuestionPhaseException();
			}
			
			if (!playerId.equals(lobby.getQuestionWriterId())) {
				throw new NotQuestionWriterException();
			}
			
			if (lobby.getQuestion() != null) {
				throw new QuestionAlreadyWrittenException();
			}
			
			lobby.setQuestion(question);
		}
	}
	
	public void deleteQuestion(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			lobby.setQuestion(null);
		}
	}
	
	
	
}
