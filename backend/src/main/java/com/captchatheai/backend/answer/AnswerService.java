package com.captchatheai.backend.answer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.answer.exception.AnswerAlreadyWrittenException;
import com.captchatheai.backend.answer.exception.CannotAnswerAsQuestionWriterException;
import com.captchatheai.backend.answer.exception.GetAnswersDeniedException;
import com.captchatheai.backend.answer.exception.InvalidAnswerException;
import com.captchatheai.backend.answer.exception.NotAnswerPhaseException;
import com.captchatheai.backend.answer.exception.SendAnswerDeniedException;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyLookup;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerLookup;
import com.captchatheai.backend.player.PlayerStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The AnswerService class allows players to get all the answers in a lobby and
 * send an answer to a lobby. Also enables the deletion of all the answers in a
 * lobby.
 * 
 * @author Alex Liu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {

	/** Maximum length for a submitted answer */
	private final static int MAX_ANSWER_LENGTH = 100;

	private final LobbyLookup lobbyLookup;

	private final PlayerLookup playerLookup;

	private final ApplicationEventPublisher eventPublisher;

	/**
	 * The getAnswers method gets all the answers in a given lobby, and returns it
	 * as an AnswersResponseDto.
	 * 
	 * @param lobbyId the lobbyId to get the answers from
	 * @return the answers in a given lobby as an AnswersResponseDto
	 * @throws GetAnswersDeniedException if the lobby is not in the DISCUSS or
	 *                                   VOTING phase
	 */
	public AnswersResponse getAnswers(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			if (lobby.getPhase() != LobbyPhase.DISCUSS && lobby.getPhase() != LobbyPhase.VOTING) {

				throw new GetAnswersDeniedException("Lobby Id: " + lobbyId + "Lobby Round: " + lobby.getRoundCount()
						+ ", Lobby Phase: " + lobby.getPhase()

						+ ", Get answers was denied: Lobby was not in discuss or voting phase.");
			}

			Map<UUID, String> answersByPlayerId = lobby.getAnswersById();
			// Map all the current answers in the lobby as an AnswerDto and make a new list
			// to store them.
			List<AnswerResponse> answers = answersByPlayerId.entrySet().stream().map((entry) -> {
				Player player = lobby.getPlayersById().get(entry.getKey());

				return new AnswerResponse(player.getName(), player.getAvatar(), entry.getValue());

			}).toList();

			log.info("Lobby Id: {}, Lobby Round: {}, Get answers was successful.", lobbyId, lobby.getRoundCount());
			return new AnswersResponse(answers);

		}
	}

	/**
	 * The sendAnswer method allows a player to send their answer to a given lobby.
	 * 
	 * @param lobbyId  the lobbyId to send their answer to
	 * @param playerId the playerId of the player
	 * @param answer   the answer that the player wants to send
	 * @throws NotAnswerPhaseException               if player sends an answer thats
	 *                                               not during the answer phase
	 * @throws SendAnswerDeniedException             if the player is not alive when
	 *                                               they send their answer
	 * @throws CannotAnswerAsQuestionWriterException if the player sends an answer
	 *                                               while being the question writer
	 * @throws AnswerAlreadyWrittenException         if the player has already sent
	 *                                               an answer in that round
	 * @throws InvalidAnswerException                if the answer sent was over 100
	 *                                               characters or blank.
	 */
	public void sendAnswer(int lobbyId, UUID playerId, String answer) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			if (lobby.getPhase() != LobbyPhase.ANSWER) {

				throw new NotAnswerPhaseException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ ", Lobby Phase: " + lobby.getPhase() + ", Player Id: " + playerId
						+ ", Send answer denied: Lobby was not in the answer phase.");
			}

			PlayerStatus playerStatus = playerLookup.getPlayerById(lobbyId, playerId).getStatus();
			if (playerStatus != PlayerStatus.ALIVE) {

				throw new SendAnswerDeniedException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ ", Player State: " + playerStatus + ", Player Id: " + playerId
						+ ", Send answer denied: player was not in the alive state.");
			}

			if (lobby.getQuestionWriterId().equals(playerId)) {
				throw new CannotAnswerAsQuestionWriterException(
						"Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount() + ", Player Id: " + playerId
								+ ", Send answer denied: Player is the question writer.");
			}

			if (lobby.getAnswersById().containsKey(playerId)) {
				throw new AnswerAlreadyWrittenException(
						"Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount() + ", Player Id: " + playerId
								+ ", Send answer denied: Player already sent an answer.");
			}

			if (answer == null || answer.length() > MAX_ANSWER_LENGTH || answer.isBlank()) {
				throw new InvalidAnswerException("Lobby Id: " + lobbyId + ", Player Id: " + playerId
						+ ", Send answer denied: Answer is over " + MAX_ANSWER_LENGTH + " characters or is blank.");
			}

			lobby.getAnswersById().put(playerId, answer);
			log.info("Lobby Id: {}, Lobby Round: {}, Player Id: {}, Answer was successfully sent.", lobbyId,
					lobby.getRoundCount(), playerId);

			eventPublisher.publishEvent(new SubmittedAnswerEvent(lobbyId));

		}
	}

	/**
	 * The deleteAnswers method is a cleanup method to delete all the answers in a
	 * given lobby.
	 * 
	 * @param lobbyId the lobbyId to delete all the answers from
	 */
	public void deleteAnswers(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			lobby.getAnswersById().clear();
			log.info("Lobby Id: {}, Lobby Round: {}, Answers were deleted.", lobbyId, lobby.getRoundCount());
		}
	}

}
