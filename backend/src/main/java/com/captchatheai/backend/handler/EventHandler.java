package com.captchatheai.backend.handler;

import java.util.Map;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.captchatheai.backend.answer.SubmittedAnswerEvent;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyLookup;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.PlayerAddedEvent;
import com.captchatheai.backend.player.PlayerRemovedEvent;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerStatus;
import com.captchatheai.backend.question.QuestionService;
import com.captchatheai.backend.vote.SubmittedVoteEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The EventHandler class handles events such as submitted answers, added
 * players, removed players, and submitted votes.
 * 
 * @author Alex Liu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandler {

	private final LobbyLookup lobbyLookup;

	private final LobbyService lobbyService;

	private final QuestionService questionService;

	/**
	 * The handleSubmittedAnswerEvent method checks if all players have answered and
	 * transitions phases to the discuss announcement if its true.
	 * 
	 * @param submittedAnswerEvent the event to handle
	 */
	@EventListener
	public void handleSubmittedAnswerEvent(SubmittedAnswerEvent submittedAnswerEvent) {
		int lobbyId = submittedAnswerEvent.lobbyId();
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			// If all players that are alive have answered, we need to go to the next phase
			// immediately. Keep in mind that the question writer
			// cannot answer their own question, so we subtract by 1.
			if (lobby.getAnswersById().size() == lobby.getAlivePlayerCount() - 1) {
				log.info("Lobby Id: {}, Lobby Round: {}, Players submitted all answers early.", lobbyId,
						lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.DISCUSS_ANNOUNCEMENT);
			}
		}
	}

	/**
	 * The handleSubmittedVoteEvent method checks if all players have voted and
	 * transitions phases to the reveal announcement if its true.
	 * 
	 * @param submittedVoteEvent the event to handle
	 */
	@EventListener
	public void handleSubmittedVoteEvent(SubmittedVoteEvent submittedVoteEvent) {
		int lobbyId = submittedVoteEvent.lobbyId();
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			Map<UUID, UUID> voteTargetByVoter = lobby.getVoteTargetByVoter();
			// If all players have finished voting, we can skip ahead to the next phase.
			if (voteTargetByVoter.size() == lobby.getAlivePlayerCount()) {
				log.info("Lobby Id: {}, Lobby Round: {}, Voting has finished early.", lobbyId, lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL_ANNOUNCEMENT);
			}
		}
	}

	/**
	 * The handlePlayerAddedEvent method transitions the lobby to the starting phase
	 * once enough players have joined.
	 * 
	 * @param playerAddedEvent the event to handle
	 */
	@EventListener
	public void handlePlayerAddedEvent(PlayerAddedEvent playerAddedEvent) {
		int lobbyId = playerAddedEvent.lobbyId();
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			lobbyService.broadcastLobbyState(lobbyId);
			// If the lobby is in intermission and our player count is 3, we need to
			// transition to the starting phase.
			if (lobby.getPhase() == LobbyPhase.INTERMISSION
					&& lobby.getPlayerCount() == PlayerService.MIN_PLAYER_COUNT) {
				log.info("Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Lobby has enough players, lobby has started.",
						lobbyId, lobby.getPhase(), lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.STARTING);

			}
		}
	}

	/**
	 * The handlePlayerRemovedEvent method handles the removal of a player. It
	 * specifically handles these conditions, too many players leaving after game
	 * has started, player that left being the question writer, too many players
	 * leaving while game is starting, and player leaving during voting phase.
	 * 
	 * @param playerRemovedEvent the event to handle
	 */
	@EventListener
	public void handlePlayerRemovedEvent(PlayerRemovedEvent playerRemovedEvent) {
		int lobbyId = playerRemovedEvent.lobbyId();
		UUID playerId = playerRemovedEvent.playerId();
		PlayerStatus playerStatus = playerRemovedEvent.initialPlayerStatus();
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			lobbyService.broadcastLobbyState(lobbyId);

			// If the game has already started and too many players have left, go to the not
			// enough players phase.
			if (lobby.getPhase() != LobbyPhase.INTERMISSION && lobby.getPhase() != LobbyPhase.STARTING
					&& lobby.getPhase() != LobbyPhase.ELIMINATION && lobby.getPhase() != LobbyPhase.AI_PLAYER_WON
					&& lobby.getPhase() != LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND
					&& lobby.getPhase() != LobbyPhase.HUMAN_PLAYERS_WON
					&& lobby.getPhase() != LobbyPhase.NOT_ENOUGH_PLAYERS
					&& lobby.getAlivePlayerCount() == PlayerService.END_GAME_EARLY_PLAYER_COUNT) {
				log.info("Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Too many players left after game started.",
						lobbyId, lobby.getPhase(), lobby.getRoundCount(), playerId);
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.NOT_ENOUGH_PLAYERS);

			}

			// If the player that left was the question writer, choose another question
			// writer.
			if (lobby.getPhase() == LobbyPhase.QUESTION && playerId.equals(lobby.getQuestionWriterId())) {
				lobby.setEliminatedPlayerId(playerId);
				questionService.setNextQuestionWriter(lobbyId);
				log.info(
						"Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Player Id: {}, Question writer left during the question phase.",
						lobbyId, lobby.getPhase(), lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.QUESTION_DISCONNECT);

			}

			// If the lobby phase is STARTING and the amount of players is less than 3, go
			// back to intermission.
			if (lobby.getPhase() == LobbyPhase.STARTING && lobby.getPlayerCount() < PlayerService.MIN_PLAYER_COUNT) {
				log.info(
						"Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Too many players left while the game was in the process of starting.",
						lobbyId, lobby.getPhase(), lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.INTERMISSION);

			}

			// If a player left during the voting phase and that player was alive, we need
			// to restart the voting phase.
			if (lobby.getPhase() == LobbyPhase.VOTING && playerStatus == PlayerStatus.ALIVE) {
				lobby.setEliminatedPlayerId(playerId);
				log.info(
						"Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Player Id: {}, Alive player left during voting phase, restarting vote phase.",
						lobbyId, lobby.getPhase(), lobby.getRoundCount(), playerId);
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.VOTING_RESTART);

			}
		}
	}

}
