package com.captchatheai.backend.question;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerState;
import com.captchatheai.backend.question.exception.GetQuestionDeniedException;
import com.captchatheai.backend.question.exception.InvalidQuestionException;
import com.captchatheai.backend.question.exception.NotQuestionPhaseException;
import com.captchatheai.backend.question.exception.NotQuestionWriterException;
import com.captchatheai.backend.question.exception.QuestionAlreadyWrittenException;
import com.captchatheai.backend.question.exception.SendQuestionDeniedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The QuestionService class allows players to get the current question in the
 * lobby, and send their question if they are the question writer. It also sets
 * the next question writer, and can delete the question in a lobby.
 * 
 * @author Alex Liu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

	/** Maximum length for a question */
	private final static int MAX_QUESTION_LENGTH = 100;

	private final LobbyService lobbyService;

	private final PlayerService playerService;

	/**
	 * The getQuestion method gets the current question in a given lobby.
	 * 
	 * @param lobbyId the lobbyId to get the question from
	 * @return the question from the lobby
	 * @throws GetQuestionDeniedException if the lobby if not in the ANSWER,
	 *                                    DISCUSS, or VOTING phase
	 */
	public QuestionResponse getQuestion(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			Player questionWriter = playerService.getPlayerById(lobbyId, lobby.getQuestionWriterId());
			if (lobby.getPhase() != LobbyPhase.ANSWER && lobby.getPhase() != LobbyPhase.DISCUSS
					&& lobby.getPhase() != LobbyPhase.VOTING) {

				throw new GetQuestionDeniedException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ ", Lobby Phase: " + lobby.getPhase()
						+ ", Get Question Denied: Lobby is not in the ANSWER, DISCUSS, or VOTING phase.");

			}
			log.info("Lobby Id: {}, Lobby Round: {}, Get Question was successfully ran.", lobbyId,
					lobby.getRoundCount());
			return new QuestionResponse(questionWriter.getName(), questionWriter.getAvatar(), lobby.getQuestion());
		}

	}

	/**
	 * The setNextQuestionWriter method sets the next question writer for a lobby.
	 * 
	 * @param lobbyId the lobbyId to set the question writer
	 */
	public void setNextQuestionWriter(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			// We call getPlayerIds (stored as an ArrayList) so that we can maintain the
			// player ordering.
			List<UUID> playerIds = lobby.getPlayerIds();

			// If the game just started, indexOf will return -1 since the current question
			// writer is null.
			int index = playerIds.indexOf(lobby.getQuestionWriterId());

			// Keep looping until we find a player that is ALIVE.
			UUID newQuestionWriterId = playerIds.get((++index) % playerIds.size());
			while (playerService.getPlayerById(lobbyId, newQuestionWriterId).getState() != PlayerState.ALIVE) {
				newQuestionWriterId = playerIds.get((++index) % playerIds.size());
			}

			lobby.setQuestionWriterId(newQuestionWriterId);
			log.info(
					"Lobby Id: {}, Lobby Round: {}. New Question Writer Id: {}, New question writer was successfully set.",
					lobbyId, lobby.getRoundCount(), newQuestionWriterId);
			playerService.broadcastPlayers(lobbyId);
		}
	}

	/**
	 * The sendQuestion method allows the question writer to send their question.
	 * 
	 * @param lobbyId  the lobbyId the question was sent from
	 * @param playerId the playerId of the question writer
	 * @param question the question to be sent
	 * 
	 * @throws NotQuestionPhaseException       if the lobby is not in question phase
	 * @throws SendQuestionDeniedException     if the player is not alive
	 * @throws NotQuestionWriterException      if the player is not the question
	 *                                         writer
	 * @throws QuestionAlreadyWrittenException if the player already wrote their
	 *                                         question
	 * @throws InvalidQuestionException        if the question is over 100
	 *                                         characters or is blank
	 */
	public void sendQuestion(int lobbyId, UUID playerId, String question) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			if (lobby.getPhase() != LobbyPhase.QUESTION) {
				throw new NotQuestionPhaseException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ ", Lobby Phase: " + lobby.getPhase() + "Player Id: " + playerId
						+ ", Send question denied: Lobby is not in QUESTION phase.");
			}

			PlayerState playerState = playerService.getPlayerById(lobbyId, playerId).getState();
			if (playerState != PlayerState.ALIVE) {
				throw new SendQuestionDeniedException(
						"Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount() + "Player Id: " + playerId
								+ "Player State: " + playerState + ", Send question denied: Player is not alive.");
			}

			if (!playerId.equals(lobby.getQuestionWriterId())) {
				throw new NotQuestionWriterException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ "Player Id: " + playerId + ", Send question denied: Player is not the question writer.");
			}

			if (lobby.getQuestion() != null) {
				throw new QuestionAlreadyWrittenException(
						"Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount() + "Player Id: " + playerId
								+ ", Send question denied: Player already wrote their question.");
			}

			if (question == null || question.length() > MAX_QUESTION_LENGTH || question.isBlank()) {
				throw new InvalidQuestionException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ "Player Id: " + playerId + ", Send question denied: Question is over " + MAX_QUESTION_LENGTH
						+ " characters or is blank.");
			}

			lobby.setQuestion(question);
			log.info("Lobby Id: {}, Lobby Round: {}, Player Id: {}, Question was successfully sent.", lobbyId,
					lobby.getRoundCount(), playerId);
		}
	}

	/**
	 * The deleteQuestion method deletes a question for a given lobby.
	 * 
	 * @param lobbyId the lobbyId to delete the question
	 */
	public void deleteQuestion(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			lobby.setQuestion(null);
			log.info("Lobby Id: {}, Lobby Round: {}, Question was successfully deleted.", lobbyId,
					lobby.getRoundCount());
		}
	}

}
