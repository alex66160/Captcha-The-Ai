package com.captchatheai.backend.question;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyRepository;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.question.exception.NotQuestionPhaseException;
import com.captchatheai.backend.question.exception.NotQuestionWriterException;
import com.captchatheai.backend.question.exception.QuestionAlreadyWrittenException;
import com.captchatheai.backend.question.exception.QuestionNotAvailableException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

	
	
	
	private final LobbyService lobbyService;
	
	
	public QuestionDto getQuestion(String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			Player questionWriter = lobbyService.getPlayerById(lobbyId, lobby.getQuestionWriterId());
			if (lobby.getPhase() != LobbyPhase.ANSWER && 
					lobby.getPhase() != LobbyPhase.DISCUSS && 
					lobby.getPhase() != LobbyPhase.VOTING) {
				
				throw new QuestionNotAvailableException();
				
			}
			
			
			return new QuestionDto(questionWriter.getName(), questionWriter.getAvatar(), lobby.getQuestion());
		}
		
		
	}
	
	
	public void sendQuestion(String lobbyId, UUID playerId, String question) {
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
	
	public void deleteQuestion(String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			lobby.setQuestion(null);
		}
	}
	
	
	
}
