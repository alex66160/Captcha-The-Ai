package com.captchatheai.backend.lobby;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.ai.AiService;
import com.captchatheai.backend.ai.ScheduledAiEvent;
import com.captchatheai.backend.answer.AnswerService;
import com.captchatheai.backend.lobby.exception.IncorrectLobbyPasswordException;
import com.captchatheai.backend.lobby.exception.LobbyErrorType;
import com.captchatheai.backend.lobby.exception.LobbyErrorTypeResponse;
import com.captchatheai.backend.lobby.exception.LobbyFullException;
import com.captchatheai.backend.lobby.exception.LobbyNotFoundException;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerLookup;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerStatus;
import com.captchatheai.backend.question.QuestionService;
import com.captchatheai.backend.vote.VoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The LobbyService class allows players to get the current lobby state, join a
 * random lobby, join a lobby by id, create lobbies, and leave lobbies. It also
 * broadcasts lobby and player stats, and contains the actual runner for the
 * lobbies themselves using a scheduled 0.5 second tick checker to check for
 * lobbies that need to advance phases. The actual handler code for expired
 * phases is in PhaseExpiredHandler.
 * 
 * @author Alex Liu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyService {
	private final LobbyRepository lobbyRepository;

	private final LobbyLookup lobbyLookup;

	private final PlayerService playerService;

	private final PlayerLookup playerLookup;

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final VoteService voteService;
	private final AiService aiService;

	private final ApplicationEventPublisher eventPublisher;
	private final SimpMessagingTemplate messagingTemplate;

	/** The max players that a lobby can have */
	private final static int MAX_PLAYERS = 8;

	/**
	 * The broadcastTotalPlayerCountAndLobbyCount method broadcasts the lobby stats
	 * every 20 seconds.
	 * 
	 */
	@Scheduled(fixedRate = 20000)
	public void broadcastTotalPlayerCountAndLobbyCount() {
		int totalPlayerCount = lobbyRepository.findAll().stream().mapToInt((lobby) -> lobby.getPlayerCount()).sum();
		int totalLobbyCount = (int) lobbyRepository.findAll().stream().count();
		messagingTemplate.convertAndSend("/topic/stats", new StatsBroadcast(totalPlayerCount, totalLobbyCount));
	}

	/**
	 * The lobbyRunner method is responsible for checking all the lobbies to see if
	 * any of their phases has expired, and if any the lobbies have scheduled ai
	 * events to execute.
	 */
	@Scheduled(fixedRate = 500)
	public void lobbyRunner() {
		for (Lobby lobby : lobbyRepository.findAll()) {
			synchronized (lobby) {
				if (lobby.getPhaseEndTime().isBefore(Instant.now())) {
					eventPublisher.publishEvent(new LobbyPhaseExpiredEvent(lobby.getId()));
				}

				ScheduledAiEvent scheduledAiEvent = lobby.getScheduledAiEvent();

				if (scheduledAiEvent != null && !scheduledAiEvent.isProcessing()
						&& scheduledAiEvent.getTimeToExecute().isBefore(Instant.now())) {
					// We need to set processing to true here instead of in handleScheduledAiEvent
					// as handleScheduledAiEvent is async.
					scheduledAiEvent.setProcessing(true);

					aiService.handleScheduledAiEvent(lobby.getId(), scheduledAiEvent);

				}
			}
		}

	}

	/**
	 * The getLobbyState method broadcasts the lobby state to a given player based
	 * off their sessionId.
	 * 
	 * @param lobbyId   the lobby to get the lobby state from
	 * @param sessionId the players sessionId to broadcast to
	 */
	public void getLobbyState(int lobbyId, String sessionId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);

		UUID playerId = playerLookup.getPlayerIdBySessionId(lobbyId, sessionId);

		LobbyState lobbyState = new LobbyState(lobby.getPhase(), lobby.getPhaseEndTime(), lobby.getRoundCount(),
				playerService.getPlayers(lobbyId, playerId));

		messagingTemplate.convertAndSend("/queue/lobbies/" + lobbyId + "/state/" + sessionId, lobbyState);
		log.info("LOBBY STATE SENT OUT FOR SESSIONID: {}", sessionId);

	}

	/**
	 * The broadcastLobbyState methods broadcasts the lobby state to all players of
	 * a given lobby.
	 * 
	 * @param lobbyId the lobby to broadcast the lobby state of to all the players
	 */
	public void broadcastLobbyState(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();

			// Filter out disconnected player ids because they are not in the lobby anymore.
			playerIdsBySessionId.entrySet().stream()
					.filter((entry) -> playerLookup.getPlayerById(lobbyId, entry.getValue())
							.getStatus() != PlayerStatus.DISCONNECTED)
					.forEach((entry) -> getLobbyState(lobbyId, entry.getKey()));

		}

	}

	/**
	 * The joinLobby method allows a player to join a randomized lobby, and it first
	 * filters based off of lobbies that arent full, and then checks if there are
	 * any lobbies currently in the INTERMISSION or STARTING phase so that players
	 * dont need to wait to play. Otherwise, it just makes a new public lobby and
	 * puts the player in that lobby.
	 * 
	 * @param sessionId the sessionId of the player that wants to join a lobby
	 */
	public void joinLobby(String sessionId) {
		// We use a while true loop to retry another lobby to join in case our max
		// players check is invalidated by the time we synchronize on the lobby to join.
		while (true) {
			// First filter by lobbies that are public (not password protected) and arent
			// full.
			List<Lobby> lobbiesToJoin = lobbyRepository.findAll().stream()
					.filter((lobby) -> lobby.getPlayerCount() <= MAX_PLAYERS && lobby.getPassword() == null).toList();
			Lobby lobbyToJoin;
			// If no available lobbies exist, just make a new one.
			if (lobbiesToJoin.isEmpty()) {
				log.info("NO LOBBIES AVAILABL CREATING NEW ONE{}", sessionId);
				createLobby(sessionId, null);
				break;

			} else {
				// Set the lobbyToJoin from a random lobby that isnt full as a backup in case
				// the next filter returns empty
				lobbyToJoin = lobbiesToJoin.get(ThreadLocalRandom.current().nextInt(lobbiesToJoin.size()));
				// Here we attempt to filter further so that players can join games quicker.
				lobbiesToJoin = lobbiesToJoin.stream().filter((lobby) -> lobby.getPhase() == LobbyPhase.INTERMISSION
						|| lobby.getPhase() == LobbyPhase.STARTING).toList();

				// If a lobby exists in intermission or start, set it equal to a random lobby
				// there.
				if (!lobbiesToJoin.isEmpty()) {
					lobbyToJoin = lobbiesToJoin.get(ThreadLocalRandom.current().nextInt(lobbiesToJoin.size()));

				}

			}

			synchronized (lobbyToJoin) {
				if (lobbyToJoin.getPlayerCount() >= MAX_PLAYERS) {
					// Retry if the max players is violated by the time we tried to sync.
					continue;
				}

				playerService.addPlayer(lobbyToJoin.getId(), sessionId);
				messagingTemplate.convertAndSend("/queue/lobbies/join/" + sessionId,
						new LobbyIdResponse(lobbyToJoin.getId()));
				log.info("JOIN RESPONSE SENT OUT FOR SESSIONID {} LOBBYID {}", sessionId, lobbyToJoin.getId());
				break;

			}
		}

	}

	/**
	 * The getIsLobbyPasswordProtected method checks whether or not a given lobby is
	 * password protected.
	 * 
	 * @param lobbyId the lobby to check if its password protected
	 * @return whether or not the lobby is password protected or not
	 */
	public IsLobbyPasswordProtectedResponse getIsLobbyPasswordProtected(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			return new IsLobbyPasswordProtectedResponse(lobby.getPassword() != null);
		}

	}

	/**
	 * The joinLobbyById method allows a player to join a lobby through a lobbyId.
	 * 
	 * @param lobbyId   the lobby the player wants to join
	 * @param sessionId the sessionId of the player
	 * @param password  the password of the lobby
	 * @throws LobbyNotFoundException          if the lobbyId does not exist
	 * @throws IncorrectLobbyPasswordException if the password is incorrect
	 * @throws LobbyFullException              if the lobby to join is full.
	 */
	public void joinLobbyById(int lobbyId, String sessionId, String password) {
		Lobby lobby;
		try {
			lobby = lobbyLookup.getLobbyById(lobbyId);

		} catch (LobbyNotFoundException e) {
			messagingTemplate.convertAndSend("/queue/lobbies/errors/" + sessionId,
					new LobbyErrorTypeResponse(LobbyErrorType.LOBBY_NOT_FOUND));
			// Rethrow exception back up so our exception handler can properly log it.
			throw e;
		}

		synchronized (lobby) {

			if (lobby.getPassword() != null && !lobby.getPassword().equals(password)) {
				messagingTemplate.convertAndSend("/queue/lobbies/errors/" + sessionId,
						new LobbyErrorTypeResponse(LobbyErrorType.LOBBY_INCORRECT_PASSWORD));
				throw new IncorrectLobbyPasswordException("Lobby Id: " + lobbyId + ", Session Id: " + sessionId
						+ ", Join lobby by id denied: Incorrect password entered.");
			}

			if (lobby.getPlayerCount() >= MAX_PLAYERS) {
				messagingTemplate.convertAndSend("/queue/lobbies/errors/" + sessionId,
						new LobbyErrorTypeResponse(LobbyErrorType.LOBBY_FULL));
				throw new LobbyFullException("Lobby Id: " + lobbyId + ", Session Id: " + sessionId
						+ ", Join lobby by id denied: Lobby is full.");
			}

			playerService.addPlayer(lobby.getId(), sessionId);
			messagingTemplate.convertAndSend("/queue/lobbies/join/" + sessionId, new LobbyIdResponse(lobby.getId()));
			log.info("JOIN RESPONSE SENT OUT FOR SESSIONID {} LOBBYID {}", sessionId, lobby.getId());

		}
	}

	/**
	 * The createLobby method allows a player to create a new lobby, and allows them
	 * to make it password protected.
	 * 
	 * @param sessionId the player trying to make a new lobby
	 * @param password  the password for the lobby
	 */
	public void createLobby(String sessionId, String password) {
		while (true) {
			Lobby lobby = new Lobby(password);
			synchronized (lobby) {
				// If create returns true, it means a lobby with a duplicate lobbyId was not
				// found. Otherwise, we need to retry to create a lobby with a different id.
				if (lobbyRepository.create(lobby)) {

					transitionToPhase(lobby.getId(), LobbyPhase.INTERMISSION);

					Player aiPlayer = playerService.addPlayer(lobby.getId(), null);
					lobby.setAiPlayerId(aiPlayer.getId());

					playerService.addPlayer(lobby.getId(), sessionId);
					messagingTemplate.convertAndSend("/queue/lobbies/join/" + sessionId,
							new LobbyIdResponse(lobby.getId()));

					log.info("Lobby Id: {}, sessionId: {}, New lobby was successfully created.", lobby.getId(),
							sessionId);

					break;
				}
			}
		}

	}

	/**
	 * The leaveLobby method allows a player to leave a lobby.
	 * 
	 * @param lobbyId  the lobby to leave from
	 * @param playerId the player to leave
	 */
	public void leaveLobby(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			playerService.removePlayer(lobbyId, playerId);
		}

	}

	/**
	 * The deleteLobby method deletes a given lobby.
	 * 
	 * @param lobbyId the lobby to delete
	 */
	public void deleteLobby(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			lobbyRepository.deleteById(lobbyId);
		}

	}

	/**
	 * The prepareLobbyForNextGame method prepares a lobby for another game by
	 * clearing votes, questions, answers, votes, round count, chat history, and
	 * clears the player identities.
	 * 
	 * @param lobbyId
	 */
	public void prepareLobbyForNextGame(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			voteService.clearVotes(lobbyId);
			questionService.deleteQuestion(lobbyId);
			answerService.deleteAnswers(lobbyId);
			playerService.clearPlayerIdentities(lobbyId);
			playerService.removeDisconnectedPlayers(lobbyId);
			lobby.setScheduledAiEvent(null);
			lobby.setQuestionWriterId(null);
			lobby.setEliminatedPlayerId(null);
			lobby.getChatHistory().clear();
			lobby.getGameHistory().clear();
			lobby.setGameStartTime(null);
			lobby.setRoundCount(1);

			log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Lobby is prepared for next game.", lobbyId,
					lobby.getRoundCount(), lobby.getPhase());

		}
	}

	/**
	 * The prepareLobbyForNextRound method prepares the lobby for another round by
	 * incrementing the round counter and clearing votes, questions, answers, and
	 * disconnected players.
	 * 
	 * @param lobbyId the lobby to prepare for the next round
	 */
	public void prepareLobbyForNextRound(int lobbyId) {

		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			voteService.clearVotes(lobbyId);
			questionService.deleteQuestion(lobbyId);
			answerService.deleteAnswers(lobbyId);
			playerService.removeDisconnectedPlayers(lobbyId);
			lobby.setRoundCount(lobby.getRoundCount() + 1);
			log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Lobby is prepared for next round.", lobbyId,
					lobby.getRoundCount(), lobby.getPhase());
		}

	}

	/**
	 * The transitionToPhase method transitions a lobby to a given phase by setting
	 * the phase, setting the end time for that phase, and broadcasting the updated
	 * lobby state.
	 * 
	 * @param lobbyId the lobbyId to transition
	 * @param phase   the phase to transition to
	 */
	public void transitionToPhase(int lobbyId, LobbyPhase phase) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			lobby.setPhase(phase);
			lobby.setPhaseEndTime(Instant.now().plusSeconds(phase.getDuration()));
			broadcastLobbyState(lobbyId);
			log.info(
					"Lobby Id: {}, Lobby Round: {}, Old Lobby Phase: {}, New Lobby Phase: {}, Lobby has successfully transitioned phases.",
					lobbyId, lobby.getRoundCount(), phase, lobby.getPhase());
		}
	}

}
