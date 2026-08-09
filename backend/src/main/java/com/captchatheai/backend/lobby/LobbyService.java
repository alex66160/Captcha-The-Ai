package com.captchatheai.backend.lobby;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.answer.AnswerService;
import com.captchatheai.backend.handler.PhaseExpiredHandler;
import com.captchatheai.backend.lobby.exception.IncorrectLobbyPasswordException;
import com.captchatheai.backend.lobby.exception.LobbyErrorType;
import com.captchatheai.backend.lobby.exception.LobbyErrorTypeResponse;
import com.captchatheai.backend.lobby.exception.LobbyFullException;
import com.captchatheai.backend.lobby.exception.LobbyNotFoundException;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerStatus;
import com.captchatheai.backend.question.QuestionService;
import com.captchatheai.backend.vote.VoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyService {

	private final LobbyRepository lobbyRepository;

	private final PhaseExpiredHandler phaseExpiredHandler;
	private final PlayerService playerService;

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final VoteService voteService;
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
		messagingTemplate.convertAndSend("topic/stats", new StatsBroadcast(totalPlayerCount, totalLobbyCount));
	}

	/**
	 * The lobbyRunner method is responsible for checking all the lobbies to see if
	 * any of their phases has expired, and if any the lobbies have scheduled events
	 * to execute.
	 */
	@Scheduled(fixedRate = 500)
	public void lobbyRunner() {
		for (Lobby lobby : lobbyRepository.findAll()) {
			synchronized (lobby) {
				if (lobby.getPhaseEndTime().isBefore(Instant.now())) {
					advancePhase(lobby.getId());
				}

				// add code to check if the scheduledai event is ready to execute

			}
		}

	}

	public Lobby getLobbyById(int id) {
		return lobbyRepository.findById(id).orElseThrow(() -> new LobbyNotFoundException(id));
	}

	public void getLobbyState(int id, String sessionId) {
		Lobby lobby = getLobbyById(id);

		UUID playerId = playerService.getPlayerIdBySessionId(id, sessionId);

		LobbyState lobbyState = new LobbyState(lobby.getPhase(), lobby.getPhaseEndTime(), lobby.getRoundCount(),
				playerService.getPlayers(id, playerId));

		messagingTemplate.convertAndSend("/queue/lobbies/" + id + "/lobbyState/" + sessionId, lobbyState);

	}

	public void broadcastLobbyState(int lobbyId) {
		Lobby lobby = getLobbyById(lobbyId);
		synchronized (lobby) {
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();

			playerIdsBySessionId.entrySet().stream()
					.filter((entry) -> playerService.getPlayerById(lobbyId, entry.getValue())
							.getStatus() != PlayerStatus.DISCONNECTED)
					.forEach((entry) -> getLobbyState(lobbyId, entry.getKey()));

		}

	}

	public void joinLobby(String sessionId) {
		while (true) {
			// first check if a lobby exists that isnt full and is a public lobby
			List<Lobby> lobbiesToJoin = lobbyRepository.findAll().stream()
					.filter((lobby) -> lobby.getPlayerCount() <= MAX_PLAYERS && lobby.getPassword() == null).toList();
			Lobby lobbyToJoin;
			// if no avaiable lobbies exist, just make a new one.
			if (lobbiesToJoin.isEmpty()) {
				createLobby(sessionId, null);
				break;

			} else {
				// set lobbytojoin from a random lobby that isnt full as a backup in case next
				// filter returns empty
				lobbyToJoin = lobbiesToJoin.get(ThreadLocalRandom.current().nextInt(lobbiesToJoin.size()));
				// attempt to filter further so that players can join games quicker.
				lobbiesToJoin = lobbiesToJoin.stream().filter((lobby) -> lobby.getPhase() == LobbyPhase.INTERMISSION
						|| lobby.getPhase() == LobbyPhase.STARTING).toList();

				// if a lobby exists in intermission or start, set it equal to a lobby there.
				if (!lobbiesToJoin.isEmpty()) {
					lobbyToJoin = lobbiesToJoin.get(ThreadLocalRandom.current().nextInt(lobbiesToJoin.size()));

				}

			}

			synchronized (lobbyToJoin) {
				if (lobbyToJoin.getPlayerCount() >= MAX_PLAYERS) {
					// retry if the max players is violated by the time we tried to sync.
					continue;
				}

				playerService.addPlayer(lobbyToJoin.getId(), sessionId);
				messagingTemplate.convertAndSend("/queue/join/" + sessionId, new LobbyIdResponse(lobbyToJoin.getId()));
				break;

			}
		}

	}

	public void joinLobbyById(int id, String sessionId, String password) {
		Lobby lobby;
		try {
			lobby = getLobbyById(id);

		} catch (LobbyNotFoundException e) {
			messagingTemplate.convertAndSend("/queue/error/" + sessionId,
					new LobbyErrorTypeResponse(LobbyErrorType.LOBBY_NOT_FOUND));
			throw e;
		}

		synchronized (lobby) {

			if (lobby.getPassword() != null && !lobby.getPassword().equals(password)) {
				messagingTemplate.convertAndSend("/queue/error/" + sessionId,
						new LobbyErrorTypeResponse(LobbyErrorType.LOBBY_INCORRECT_PASSWORD));
				throw new IncorrectLobbyPasswordException();
			}

			if (lobby.getPlayerCount() >= MAX_PLAYERS) {
				messagingTemplate.convertAndSend("/queue/error/" + sessionId,
						new LobbyErrorTypeResponse(LobbyErrorType.LOBBY_FULL));
				throw new LobbyFullException();
			}

			playerService.addPlayer(lobby.getId(), sessionId);
			messagingTemplate.convertAndSend("/queue/join/" + sessionId, new LobbyIdResponse(lobby.getId()));

		}
	}

	public void leaveLobby(int id, UUID playerId) {
		Lobby lobby = getLobbyById(id);
		synchronized (lobby) {
			playerService.removePlayer(id, playerId);
		}

	}

	public void createLobby(String sessionId, String password) {
		while (true) {

			Lobby lobby = new Lobby(password);
			synchronized (lobby) {
				if (lobbyRepository.create(lobby)) {

					transitionToPhase(lobby.getId(), LobbyPhase.INTERMISSION);

					Player aiPlayer = playerService.addPlayer(lobby.getId(), null);
					lobby.setAiPlayerId(aiPlayer.getId());
					playerService.addPlayer(lobby.getId(), sessionId);
					messagingTemplate.convertAndSend("/queue/join/" + sessionId, new LobbyIdResponse(lobby.getId()));
					break;
				}
			}
		}

	}

	public void deleteLobby(int id) {
		Lobby lobby = getLobbyById(id);
		synchronized (lobby) {
			lobbyRepository.deleteById(id);
		}

	}

	public void prepareLobbyForNextGame(int id) {
		Lobby lobby = getLobbyById(id);
		synchronized (lobby) {
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
		synchronized (lobby) {
			voteService.clearVotes(id);
			questionService.deleteQuestion(id);
			answerService.deleteAnswers(id);

			lobby.setRoundCount(lobby.getRoundCount() + 1);
		}

	}

	/**
	 * The advancePhase method advances the lobby to a new phase by calling the
	 * respective handler to handle whichever phase had just expired.
	 * 
	 * @author Alex Liu
	 * @param lobbyId the lobbyId to advance the phase on
	 */
	public void advancePhase(int lobbyId) {
		Lobby lobby = getLobbyById(lobbyId);
		synchronized (lobby) {
			switch (lobby.getPhase()) {

			case INTERMISSION -> phaseExpiredHandler.handleIntermissionExpired(lobbyId);

			case STARTING -> phaseExpiredHandler.handleStartingExpired(lobbyId);

			case INTRO -> phaseExpiredHandler.handleIntroExpired(lobbyId);

			case QUESTION_ANNOUNCEMENT -> phaseExpiredHandler.handleQuestionAnnouncementExpired(lobbyId);

			case QUESTION -> phaseExpiredHandler.handleQuestionExpired(lobbyId);

			case QUESTION_DISCONNECT -> phaseExpiredHandler.handleQuestionDisconnectExpired(lobbyId);

			case QUESTION_EMPTY -> phaseExpiredHandler.handleQuestionEmptyExpired(lobbyId);

			case ANSWER_ANNOUNCEMENT -> phaseExpiredHandler.handleAnswerAnnouncementExpired(lobbyId);

			case ANSWER -> phaseExpiredHandler.handleAnswerExpired(lobbyId);

			case DISCUSS_ANNOUNCEMENT -> phaseExpiredHandler.handleDiscussAnnouncementExpired(lobbyId);

			case DISCUSS -> phaseExpiredHandler.handleDiscussExpired(lobbyId);

			case VOTING -> phaseExpiredHandler.handleVotingExpired(lobbyId);

			case VOTING_RESTART -> phaseExpiredHandler.handleVotingRestartExpired(lobbyId);

			case REVEAL_ANNOUNCEMENT -> phaseExpiredHandler.handleRevealAnnouncementExpired(lobbyId);

			case REVEAL -> phaseExpiredHandler.handleRevealExpired(lobbyId);

			case REVEAL_TIE -> phaseExpiredHandler.handleRevealTieExpired(lobbyId);

			case ELIMINATION -> phaseExpiredHandler.handleEliminationExpired(lobbyId);

			case AI_PLAYER_WON -> phaseExpiredHandler.handleGameResultExpired(lobbyId);

			case AI_PLAYER_FAILED_TO_RESPOND -> phaseExpiredHandler.handleGameResultExpired(lobbyId);

			case HUMAN_PLAYERS_WON -> phaseExpiredHandler.handleGameResultExpired(lobbyId);

			case NOT_ENOUGH_PLAYERS -> phaseExpiredHandler.handleGameResultExpired(lobbyId);

			}
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
		Lobby lobby = getLobbyById(lobbyId);
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
