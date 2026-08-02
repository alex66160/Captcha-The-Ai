package com.captchatheai.backend.answer;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.answer.exception.AnswerAlreadyWrittenException;

import com.captchatheai.backend.answer.exception.CannotAnswerAsQuestionWriterException;

import com.captchatheai.backend.answer.exception.GetAnswersDeniedException;
import com.captchatheai.backend.answer.exception.NotAnswerPhaseException;
import com.captchatheai.backend.answer.exception.SendAnswerDeniedException;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerState;


import lombok.RequiredArgsConstructor;

/**
 * The AnswerService class allows players to get all the answers in a lobby and send an answer to a lobby.
 * Also enables the deletion of all the answers in a lobby.
 * 
 * @author Alex Liu
 */
@Service
@RequiredArgsConstructor
public class AnswerService {

	private final LobbyService lobbyService;
	
	
	private final PlayerService playerService;


	/**
	 * The getAnswers method gets all the answers in a given lobby, and
	 * returns it as an AnswersDto.
	 * 
	 * @param lobbyId the lobbyId to get the answers from
	 * @return the answers in a given lobby as an AnswersDto
	 * @throws GetAnswersDeniedException if the lobby is not in the DISCUSS or VOTING phase
	 */
	public AnswersDto getAnswers(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			

			if (lobby.getPhase() != LobbyPhase.DISCUSS && 
					lobby.getPhase() != LobbyPhase.VOTING) {

				throw new GetAnswersDeniedException();
				
			}
			
			Map<UUID, String> answersByPlayerId = lobby.getAnswersById();
			// Map all the current answers in the lobby as an AnswerDto and make a new list to store them.
			List<AnswerDto> answers = answersByPlayerId.entrySet().stream().map((entry) -> {
						Player player = lobby.getPlayersById().get(entry.getKey());
						
						return new AnswerDto(player.getName(), player.getAvatar(), entry.getValue());
								
					}).toList();
					
			return new AnswersDto(answers);
			
			
		}
	}
	
	/**
	 * The sendAnswer method allows a player to send their answer to a given lobby.
	 * 
	 * @param lobbyId the lobbyId to send their answer to
	 * @param playerId the playerId of the player
	 * @param answer the answer that the player wants to send
	 * @throws NotAnswerPhaseException if player sends an answer thats not during the answer phase
	 * @throws SendAnswerDeniedException if the player is not alive when they send their answer
	 * @throws CannotAnswerAsQuestionWriterException if the player sends an answer while being the question writer
	 * @throws AnswerAlreadyWrittenException if the player has already sent an answer in that round
	 */
	public void sendAnswer(int lobbyId, UUID playerId, String answer) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			
			if (lobby.getPhase() != LobbyPhase.ANSWER) {
				throw new NotAnswerPhaseException();
			}
			
			if (playerService.getPlayerById(lobbyId, playerId).getState() != PlayerState.ALIVE) {
				throw new SendAnswerDeniedException();
			}
			
	
			if (lobby.getQuestionWriterId().equals(playerId)) {
				throw new CannotAnswerAsQuestionWriterException();
			}
			
		
			if (lobby.getAnswersById().containsKey(playerId) ) {
				throw new AnswerAlreadyWrittenException();
			}
			
			
			
			lobby.getAnswersById().put(playerId, answer);
			
			// If all players that are alive have answered, 
			if (lobby.getAnswersById().size() == lobby.getPlayerIds().stream().filter((playerIdFromLobby) -> 
			playerService.getPlayerById(lobbyId, playerIdFromLobby).getState() == PlayerState.ALIVE).count()) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.DISCUSS_START);
			}
			
		}
	}
	
	public void deleteAnswers(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			lobby.getAnswersById().clear();
		}
	}
	
	
}
