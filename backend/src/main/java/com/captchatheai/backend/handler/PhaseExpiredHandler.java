package com.captchatheai.backend.handler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.captchatheai.backend.ai.AiEvent;
import com.captchatheai.backend.ai.ScheduledAiEvent;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyLookup;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyPhaseExpiredEvent;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.PlayerLookup;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerStatus;
import com.captchatheai.backend.question.QuestionService;
import com.captchatheai.backend.vote.VoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The PhaseExpiredHandler class handles all the logic and transitions for any
 * given lobby phase when they expire.
 * 
 * @author Alex Liu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhaseExpiredHandler {

	private final LobbyService lobbyService;

	private final LobbyLookup lobbyLookup;
	private final PlayerService playerService;
	private final PlayerLookup playerLookup;
	private final QuestionService questionService;

	private final VoteService voteService;

	@EventListener
	/**
	 * The handleLobbyPhaseExpiredEvent method advances the lobby to a new phase by
	 * calling the respective handler to handle whichever phase had just expired.
	 * 
	 * @param lobbyId the lobbyId to advance the phase on
	 */
	public void handleLobbyPhaseExpiredEvent(LobbyPhaseExpiredEvent lobbyPhaseExpiredEvent) {
		int lobbyId = lobbyPhaseExpiredEvent.lobbyId();
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			switch (lobby.getPhase()) {

			case INTERMISSION -> handleIntermissionExpired(lobbyId);

			case STARTING -> handleStartingExpired(lobbyId);

			case INTRO -> handleIntroExpired(lobbyId);

			case QUESTION_ANNOUNCEMENT -> handleQuestionAnnouncementExpired(lobbyId);

			case QUESTION -> handleQuestionExpired(lobbyId);

			case QUESTION_DISCONNECT -> handleQuestionDisconnectExpired(lobbyId);

			case QUESTION_EMPTY -> handleQuestionEmptyExpired(lobbyId);

			case ANSWER_ANNOUNCEMENT -> handleAnswerAnnouncementExpired(lobbyId);

			case ANSWER -> handleAnswerExpired(lobbyId);

			case DISCUSS_ANNOUNCEMENT -> handleDiscussAnnouncementExpired(lobbyId);

			case DISCUSS -> handleDiscussExpired(lobbyId);

			case VOTING -> handleVotingExpired(lobbyId);

			case VOTING_RESTART -> handleVotingRestartExpired(lobbyId);

			case REVEAL_ANNOUNCEMENT -> handleRevealAnnouncementExpired(lobbyId);

			case REVEAL -> handleRevealExpired(lobbyId);

			case REVEAL_TIE -> handleRevealTieExpired(lobbyId);

			case ELIMINATION -> handleEliminationExpired(lobbyId);

			case AI_PLAYER_WON -> handleGameResultExpired(lobbyId);

			case AI_PLAYER_FAILED_TO_RESPOND -> handleGameResultExpired(lobbyId);

			case HUMAN_PLAYERS_WON -> handleGameResultExpired(lobbyId);

			case NOT_ENOUGH_PLAYERS -> handleGameResultExpired(lobbyId);

			}
		}
	}

	/**
	 * The handleIntermissionExpired method kicks all human players in the lobby and
	 * then deletes the lobby itself.
	 * 
	 * @param lobbyId the lobby that had the intermission phase expired
	 */
	public void handleIntermissionExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			List<UUID> playerIdsToKick = lobby.getPlayerIds().stream()
					.filter((playerId) -> !playerId.equals(lobby.getAiPlayerId())).toList();
			playerIdsToKick.stream().forEach((playerId) -> playerService.kickPlayer(lobbyId, playerId));

			lobbyService.deleteLobby(lobbyId);
			log.info("Lobby Id: {}, Intermission phase expired, lobby was deleted.", lobbyId);

		}
	}

	/**
	 * The handleStartingExpired method assigns player identities to all the players
	 * and transitions to the INTRO phase.
	 * 
	 * @param lobbyId the lobby that had the starting phase expired
	 */
	public void handleStartingExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			playerService.assignPlayerIdentities(lobbyId);
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.INTRO);
		}
	}

	/**
	 * The handleIntroExpired method sets the next question writer and transitions
	 * to the QUESTION ANNOUNCEMENT phase.
	 * 
	 * @param lobbyId the lobby that had the intro phase expired
	 */
	public void handleIntroExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			questionService.setNextQuestionWriter(lobbyId);

			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION_ANNOUNCEMENT);
		}
	}

	/**
	 * The handleQuestionAnnouncementExpired method schedules the ai question event
	 * if the ai player is the question writer, and transitions to the QUESTION
	 * phase.
	 * 
	 * @param lobbyId the lobby that had the question announcement phase expired
	 */
	public void handleQuestionAnnouncementExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			if (lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobby.setScheduledAiEvent(new ScheduledAiEvent(AiEvent.GENERATE_QUESTION,
						Instant.now().plusSeconds(LobbyPhase.QUESTION.getDuration() / 2)));
				log.info("Lobby Id: {}, Lobby Round: {}, Scheduled Generate Question for Ai Player.", lobbyId,
						lobby.getRoundCount());
			}
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION);
		}
	}

	/**
	 * The handleQuestionExpired method checks if the question writer has made their
	 * question or not, and if not will transition to the question empty phase and
	 * kick that player if it was a human player and if it was an ai player, will
	 * transition to ai player failed to respond phase. Otherwise, it transitions to
	 * the answer announcement phase.
	 * 
	 * @param lobbyId the lobby that had the question phase expired
	 */
	public void handleQuestionExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			if ((lobby.getQuestion() == null || lobby.getQuestion().isBlank())
					&& lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				log.info("Lobby Id: {}, Lobby Round: {}. Ai Player Failed to Generate Question.", lobbyId,
						lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND);
				return;
			}
			if ((lobby.getQuestion() == null || lobby.getQuestion().isBlank())
					&& !lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobby.setEliminatedPlayerId(lobby.getQuestionWriterId());
				questionService.setNextQuestionWriter(lobbyId);
				playerService.kickPlayer(lobbyId, lobby.getEliminatedPlayerId());

				lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION_EMPTY);
				return;
			}

			lobbyService.transitionToPhase(lobbyId, LobbyPhase.ANSWER_ANNOUNCEMENT);

		}
	}

	/**
	 * The handleQuestionDisconnectExpired method deletes the question and
	 * transitions back to the question phase to allow the next question writer to
	 * make their question.
	 * 
	 * @param lobbyId the lobby that had the question disconnect phase expired
	 */
	public void handleQuestionDisconnectExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			questionService.deleteQuestion(lobbyId);
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION);

		}
	}

	/**
	 * The handleQuestionEmptyExpired method deletes the question and transitions
	 * back to the question phase to allow the next question writer to make their
	 * question.
	 * 
	 * @param lobbyId the lobby that had the question empty phase expired
	 */
	public void handleQuestionEmptyExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			questionService.deleteQuestion(lobbyId);
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION);

		}
	}

	/**
	 * The handleAnswerAnnouncementExpired method will scheduled a generate answer
	 * event if the ai player is not the question writer, and will transition to the
	 * ANSWER phase.
	 * 
	 * @param lobbyId the lobby that had the answer announcement phase expired
	 */
	public void handleAnswerAnnouncementExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			if (!lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				lobby.setScheduledAiEvent(new ScheduledAiEvent(AiEvent.GENERATE_ANSWER,
						Instant.now().plusSeconds(LobbyPhase.ANSWER.getDuration() / 2)));
				log.info("Lobby Id: {}, Lobby Round: {}, Scheduled Generate Answer for Ai Player.", lobbyId,
						lobby.getRoundCount());
			}

			lobbyService.transitionToPhase(lobbyId, LobbyPhase.ANSWER);

		}
	}

	/**
	 * The handleAnswerExpired method kicks all players that did not answer the
	 * question (other than the question writer and ai player) and if the ai player
	 * did not answer, it will transition to the ai player failed to respond phase.
	 * Otherwise, it transitions to the discuss announcement phase.
	 * 
	 * @param lobbyId the lobby that had the answer phase expired
	 */
	public void handleAnswerExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			Map<UUID, String> answersById = lobby.getAnswersById();

			List<UUID> playerIdsToKick = lobby.getPlayerIds().stream()
					// The players to kick must have not answered, be alive, and not be a question
					// writer or ai player.
					.filter((playerId) -> !answersById.containsKey(playerId)
							&& playerLookup.getPlayerById(lobbyId, playerId).getStatus() == PlayerStatus.ALIVE
							&& !playerId.equals(lobby.getAiPlayerId()) && !playerId.equals(lobby.getQuestionWriterId()))
					.toList();
			playerIdsToKick.stream().forEach((playerId) -> playerService.kickPlayer(lobbyId, playerId));

			if (!lobby.getAnswersById().containsKey(lobby.getAiPlayerId())
					&& !lobby.getAiPlayerId().equals(lobby.getQuestionWriterId())) {
				log.info("Lobby Id: {}, Lobby Round: {}. Ai Player Failed to Generate Answer.", lobbyId,
						lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND);
				return;
			}

			lobbyService.transitionToPhase(lobbyId, LobbyPhase.DISCUSS_ANNOUNCEMENT);

		}
	}

	/**
	 * The handleDiscussAnnouncementExpired method transitions to the discuss phase.
	 * 
	 * @param lobbyId the lobby that had the discuss announcement phase expired
	 */
	public void handleDiscussAnnouncementExpired(int lobbyId) {

		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			lobbyService.transitionToPhase(lobbyId, LobbyPhase.DISCUSS);
		}
	}

	/**
	 * The handleDiscussExpired method schedules a generate vote event for the ai
	 * player, and transitions to the voting phase.
	 * 
	 * @param lobbyId the lobby that had the discuss phase expired
	 */
	public void handleDiscussExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			lobby.setScheduledAiEvent(new ScheduledAiEvent(AiEvent.GENERATE_VOTE,
					Instant.now().plusSeconds(LobbyPhase.VOTING.getDuration() / 2)));
			log.info("Lobby Id: {}, Lobby Round: {}, Scheduled Generate Vote for Ai Player.", lobbyId,
					lobby.getRoundCount());
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.VOTING);
		}
	}

	/**
	 * The handleVotingExpired method kicks all players that have not voted, and
	 * transitions to ai player failed to respond phase if the ai player failed to
	 * vote. Otherwise, it transitions to REVEAL ANNOUNCEMENT phase.
	 * 
	 * @param lobbyId the lobby that had the voting phase expired
	 */
	public void handleVotingExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			Map<UUID, UUID> voteTargetByVoter = lobby.getVoteTargetByVoter();
			List<UUID> playerIdsToKick = lobby.getPlayerIds().stream()
					// Kick players that did not vote and are alive and not the ai player.
					.filter((playerId) -> !voteTargetByVoter.containsKey(playerId)
							&& playerLookup.getPlayerById(lobbyId, playerId).getStatus() == PlayerStatus.ALIVE
							&& !playerId.equals(lobby.getAiPlayerId()))
					.toList();
			playerIdsToKick.stream().forEach((playerId) -> playerService.kickPlayer(lobbyId, playerId));

			if (!voteTargetByVoter.containsKey(lobby.getAiPlayerId())) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND);
				log.info("Lobby Id: {}, Lobby Round: {}. Ai Player Failed to Generate Vote.", lobbyId,
						lobby.getRoundCount());
				return;
			}

			lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL_ANNOUNCEMENT);
		}
	}

	/**
	 * The handleVotingRestartExpired method clears all previous votes, schedules
	 * the generate vote event for the ai player, and transitions back to the voting
	 * phase.
	 * 
	 * @param lobbyId the lobby that had the voting restart phase expired
	 */
	public void handleVotingRestartExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			voteService.clearVotes(lobbyId);
			lobby.setScheduledAiEvent(new ScheduledAiEvent(AiEvent.GENERATE_VOTE,
					Instant.now().plusSeconds(LobbyPhase.VOTING.getDuration() / 2)));
			log.info("Lobby Id: {}, Lobby Round: {}, Scheduled Generate Vote for Ai Player.", lobbyId,
					lobby.getRoundCount());
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.VOTING);
		}
	}

	/**
	 * The handleRevealAnnouncementExpired method transitions to the reveal phase.
	 * 
	 * @param lobbyId the lobby that had the reveal announcement phase expired
	 */
	public void handleRevealAnnouncementExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL);

		}
	}

	/**
	 * The handleRevealExpired method calculates the votes, and if there are tied
	 * players it transitions to the reveal tie phase, otherwise it goes directly to
	 * the elimination phase.
	 * 
	 * @param lobbyId the lobby that had the reveal phase expired
	 */
	public void handleRevealExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			voteService.calculateVotes(lobbyId);
			if (lobby.getTiedPlayerIds().size() == 1) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.ELIMINATION);
			} else {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL_TIE);
			}

		}
	}

	/**
	 * The handleRevealTieExpired method transitions to the elimination phase.
	 * 
	 * @param lobbyId the lobby that had the reveal tie phase expired
	 */
	public void handleRevealTieExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			lobbyService.transitionToPhase(lobbyId, LobbyPhase.ELIMINATION);

		}
	}

	/**
	 * The handleEliminationExpired method sets the eliminated player as a
	 * spectator, and if the eliminated player was the ai, it goes to the human
	 * players won phase, and if enough players were eliminated, it goes to the ai
	 * player won phase. It will also set the eliminated player as a spectator, set
	 * the next question writer, prepare the lobby for the next round, and
	 * transition to the question announcement phase.
	 * 
	 * @param lobbyId the lobby that had the elimination phase expired
	 */
	public void handleEliminationExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			if (lobby.getEliminatedPlayerId().equals(lobby.getAiPlayerId())) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.HUMAN_PLAYERS_WON);
				return;
			}

			if (lobby.getAlivePlayerCount() <= PlayerService.END_GAME_EARLY_PLAYER_COUNT) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.AI_PLAYER_WON);
				return;
			}
			playerService.setPlayerAsSpectator(lobbyId, lobby.getEliminatedPlayerId());
			questionService.setNextQuestionWriter(lobbyId);
			lobbyService.prepareLobbyForNextRound(lobbyId);
			lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION_ANNOUNCEMENT);

		}
	}

	/**
	 * The handleGameResultExpired method handles the expiring of the AI_PLAYER_WON,
	 * AI_PLAYER_FAILED_TO_RESPOND, HUMAN_PLAYERS_WON, and NOT_ENOUGH_PLAYERS
	 * phases, and prepares the lobby for the next game and transitions to the
	 * intermission phase if too many players left and transitions to the starting
	 * phase if there are enough players to begin the game.
	 * 
	 * @param lobbyId the lobby that had a game result phase expired
	 */
	public void handleGameResultExpired(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			lobbyService.prepareLobbyForNextGame(lobbyId);
			if (lobby.getPlayerIds().size() < PlayerService.MIN_PLAYER_COUNT) {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.INTERMISSION);
			} else {
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.STARTING);
			}

		}
	}

}
