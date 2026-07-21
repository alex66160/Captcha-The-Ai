package com.captchatheai.backend.answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.answer.exception.AnswerAlreadyWrittenException;
import com.captchatheai.backend.answer.exception.AnswerNotAvailableException;
import com.captchatheai.backend.answer.exception.CannotAnswerException;
import com.captchatheai.backend.answer.exception.NotAnswerPhaseException;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.question.exception.QuestionNotAvailableException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnswerService {

private final LobbyService lobbyService;
	



	public AnswersDto getAnswers(String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			if (lobby.getPhase() != LobbyPhase.DISCUSS && 
					lobby.getPhase() != LobbyPhase.VOTING) {
				
				throw new AnswerNotAvailableException();
				
			}
			Map<UUID, String> answersByPlayerId = lobby.getAnswersById();
			
			List<AnswerDto> answers = answersByPlayerId.entrySet().stream().map((entry) -> {
						Player player = lobby.getPlayersById().get(entry.getKey());
						
						return new AnswerDto(player.getName(), player.getAvatar(), entry.getValue());
								
					}).toList();
					
			return new AnswersDto(answers);
			
			
		}
	}
	
	public void sendAnswer(String lobbyId, UUID playerId, String answer) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			if (lobby.getPhase() != LobbyPhase.ANSWER) {
				throw new NotAnswerPhaseException();
			}
			
			
			if (lobby.getQuestionWriterId().equals(playerId)) {
				throw new CannotAnswerException();
			}
			
			if (lobby.getAnswersById().containsKey(playerId)) {
				throw new AnswerAlreadyWrittenException();
			}
			
			
			
			lobby.getAnswersById().put(playerId, answer);
		}
	}
	
	public void deleteAnswers(String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			lobby.getAnswersById().clear();
		}
	}
	
	
}
