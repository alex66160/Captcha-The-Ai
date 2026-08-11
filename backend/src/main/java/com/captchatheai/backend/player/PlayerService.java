package com.captchatheai.backend.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.exception.GetAiPlayerDeniedException;
import com.captchatheai.backend.player.exception.PlayerDisconnectedException;
import com.captchatheai.backend.player.exception.PlayerNotFoundException;
import com.captchatheai.backend.question.QuestionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The PlayerService allows players to be added to a lobby or removed, and has
 * method to assign/clear player identities, and allow players to get the
 * current eliminated player or ai player. It also handles player disconnects
 * using a disconnect listener, and allows you to set a player as spectator when
 * a player is voted out.
 * 
 * @author Alex Liu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

	/**
	 * We keep a map of sessionId to lobbyId so that we know which player
	 * disconnected when our session disconnect listener receives a sessionId
	 */
	private final Map<String, Integer> lobbyIdByPlayerSessionId = new ConcurrentHashMap<>();
	private final LobbyService lobbyService;

	private final QuestionService questionService;
	private final SimpMessagingTemplate messagingTemplate;

	/**
	 * Represents the minimum amount of players to start the game.
	 */
	private final static int MIN_PLAYER_COUNT = 3;

	/**
	 * Represents the amount of players that will result in the match ending.
	 */
	public final static int END_GAME_EARLY_PLAYER_COUNT = 2;

	/**
	 * The getEliminatedPlayer method gets the player that was eliminated, and the
	 * context depends on the phase and player. The eliminated player will only be
	 * set for players voted out, a question writer that forgot to write a question,
	 * a question writer that left during the question phase, or an alive player
	 * that left during the voting phase. Its mainly used so that the frontend can
	 * display info about who was eliminated in those specific situations.
	 * 
	 * @param lobbyId the lobbyId to get the eliminated player
	 * @return the eliminated player
	 */
	public EliminatedPlayerResponse getEliminatedPlayer(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			Player eliminatedPlayer = getPlayerById(lobbyId, lobby.getEliminatedPlayerId());
			return new EliminatedPlayerResponse(eliminatedPlayer.getName(), eliminatedPlayer.getAvatar(),
					eliminatedPlayer.getId().equals(lobby.getAiPlayerId()));

		}
	}

	/**
	 * The getAiPlayer method returns the info about the aiplayer, and is used to
	 * serve as a final reveal of who the ai player was.
	 * 
	 * @param lobbyId the lobbyId to get the ai player
	 * @return the ai player info
	 */
	public AiPlayerResponse getAiPlayer(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			if (lobby.getPhase() != LobbyPhase.AI_PLAYER_WON
					&& lobby.getPhase() != LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND
					&& lobby.getPhase() != LobbyPhase.HUMAN_PLAYERS_WON
					&& lobby.getPhase() != LobbyPhase.NOT_ENOUGH_PLAYERS) {
				throw new GetAiPlayerDeniedException(
						"Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount() + ", Lobby Phase: "
								+ lobby.getPhase() + ", Get Ai Player Denied: Lobby is not in a game result phase.");
			}

			Player aiPlayer = getPlayerById(lobbyId, lobby.getAiPlayerId());
			return new AiPlayerResponse(aiPlayer.getName(), aiPlayer.getAvatar());
		}
	}

	/**
	 * The getPlayerById method gets a player using their playerId.
	 * 
	 * @param lobbyId  the lobbyId that the player belongs to
	 * @param playerId the playerId of the player we want to get
	 * @return the player
	 * @throws PlayerNotFoundException if that playerId doesnt exist in the lobbyId
	 *                                 given
	 */
	public Player getPlayerById(int lobbyId, UUID playerId) {
		Player player = lobbyService.getLobbyById(lobbyId).getPlayersById().get(playerId);
		if (player == null) {
			throw new PlayerNotFoundException(
					"Lobby Id: " + lobbyId + ", Player Id: " + playerId + ", Player Id was not found.");
		}
		return player;

	}

	/**
	 * The getPlayerByName method gets a playerId using their name.
	 * 
	 * @param lobbyId    the lobbyId that the player belongs to
	 * @param playerName the playerName of the playerId we want to get
	 * @return the playerId
	 * @throws PlayerNotFoundException if that playerName doesnt exist in the
	 *                                 lobbyId given
	 */
	public UUID getPlayerIdByName(int lobbyId, String playerName) {
		UUID playerId = lobbyService.getLobbyById(lobbyId).getPlayerIdsByName().get(playerName);
		if (playerId == null) {
			throw new PlayerNotFoundException(
					"Lobby Id: " + lobbyId + ", Player Name: " + playerName + ", Player name was not found.");
		}
		return playerId;
	}

	/**
	 * The getPlayerBySessionId method gets a playerId using their sessionId.
	 * 
	 * @param lobbyId   the lobbyId that the player belongs to
	 * @param sessionId the sessionId of the playerId we want to get
	 * @return the playerId
	 * @throws PlayerNotFoundException if that sessionId doesnt exist in the lobbyId
	 *                                 given
	 */
	public UUID getPlayerIdBySessionId(int lobbyId, String sessionId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		// This method is synchronized in particular since other controller classes need
		// to convert the session id to a player id before calling their service
		// methods.
		synchronized (lobby) {
			UUID playerId = lobby.getPlayerIdsBySessionId().get(sessionId);
			if (playerId == null) {
				throw new PlayerNotFoundException(
						"Lobby Id: " + lobbyId + ", sessionId: " + sessionId + ", sessionId was not found.");
			}
			return playerId;
		}

	}

	/**
	 * The getPlayers method returns a list of player states in a lobby from a given
	 * player's view.
	 * 
	 * @param lobbyId  the lobbyId to get the player states of
	 * @param playerId the playerId we intend to use as the view
	 * @return the list of player states
	 */
	public List<PlayerState> getPlayers(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {

			// Filter all disconnected players from the list as they are not meant to be
			// shown.
			List<UUID> playerIds = lobby.getPlayerIds().stream()
					.filter((playerIdFromLobby) -> getPlayerById(lobbyId, playerIdFromLobby)
							.getStatus() != PlayerStatus.DISCONNECTED)
					.toList();

			// Make the list of player states by mapping each player to a PlayerState
			// record.
			List<PlayerState> players = playerIds.stream().map((playerIdFromPlayerIds) -> {
				Player player = getPlayerById(lobbyId, playerIdFromPlayerIds);
				return new PlayerState(player.getName(), player.getAvatar(),
						playerId.equals(lobby.getQuestionWriterId()), player.getId().equals(playerId));

			}).toList();

			return players;
		}
	}

//	public void broadcastPlayers(int lobbyId) {
//		Lobby lobby = lobbyService.getLobbyById(lobbyId);
//		synchronized (lobby) {
//			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();
//
//			playerIdsBySessionId.entrySet().stream().forEach((entry) ->
//
//			messagingTemplate.convertAndSend("/queue/lobby/" + lobbyId + "/players/" + entry.getKey(),
//					getPlayers(lobbyId, entry.getValue())));
//
//		}
//
//	}

	/**
	 * The addPlayer method adds a player to a given lobby.
	 * 
	 * @param lobbyId   the lobbyId to add to
	 * @param sessionId the sessionId of the player we want to add.
	 * @return the player that was added
	 */
	public Player addPlayer(int lobbyId, String sessionId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			String playerName;
			PlayerAvatar playerAvatar;
			PlayerStatus playerStatus;
			// If the lobby is in the INTERMISSION or STARTING phase the player should be
			// hidden.
			if (lobby.getPhase() == LobbyPhase.INTERMISSION || lobby.getPhase() == LobbyPhase.STARTING) {
				playerName = PlayerAvatar.HIDDEN.getName();
				playerAvatar = PlayerAvatar.HIDDEN;
				playerStatus = PlayerStatus.HIDDEN;
				// Otherwise, it means the player joined in the middle of a game that started,
				// so they are a spectator.
			} else {
				playerName = PlayerAvatar.SPECTATOR.getName();
				playerAvatar = PlayerAvatar.SPECTATOR;
				playerStatus = PlayerStatus.SPECTATOR;
			}

			Player player = new Player(sessionId, playerName, playerAvatar, playerStatus, null);

			List<UUID> players = lobby.getPlayerIds();
			Map<UUID, Player> playersById = lobby.getPlayersById();
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();
			players.add(player.getId());
			playersById.put(player.getId(), player);

			// The ai player will have a null sessionid, so dont add it to the sessionId
			// related maps.
			if (sessionId != null) {
				playerIdsBySessionId.put(sessionId, player.getId());
				lobbyIdByPlayerSessionId.put(sessionId, lobbyId);

			}
			log.info(
					"Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, sessionId: {}, Player Id: {}, Player was successfully added.",
					lobbyId, lobby.getPhase(), lobby.getRoundCount(), sessionId, player.getId());
			lobbyService.broadcastLobbyState(lobbyId);

			// If the lobby is in intermission and our player count is 3, we need to
			// transition to the starting phase.
			if (lobby.getPhase() == LobbyPhase.INTERMISSION && lobby.getPlayerCount() == MIN_PLAYER_COUNT) {
				log.info("Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Lobby has enough players, lobby has started.",
						lobbyId, lobby.getPhase(), lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.STARTING);

			}

			return player;
		}
	}

	/**
	 * The removePlayer method removes a player from a lobby.
	 * 
	 * @param lobbyId  the lobbyId to remove the player from
	 * @param playerId the player to be removed
	 * @return the player that was removed
	 * @throws PlayerDisconnectedException if player to remove was already
	 *                                     disconnected
	 */
	public Player removePlayer(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {

			Player player = getPlayerById(lobbyId, playerId);
			PlayerStatus playerStatus = player.getStatus();

			if (playerStatus == PlayerStatus.DISCONNECTED) {
				throw new PlayerDisconnectedException("Lobby Id: " + lobbyId + "Player Id: " + playerId
						+ ", Remove player denied, player already disconnected.");
			}

			List<UUID> playerIds = lobby.getPlayerIds();
			Map<UUID, Player> playersById = lobby.getPlayersById();
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();

			lobbyIdByPlayerSessionId.remove(player.getSessionId());

			// If the player to be removed was HIDDEN or a SPECTATOR we can remove them from
			// our maps entirely because we don't need to use any of their player info
			// anymore.
			if (playerStatus == PlayerStatus.HIDDEN || playerStatus == PlayerStatus.SPECTATOR) {
				playerIds.remove(playerId);
				playersById.remove(playerId);
				playerIdsBySessionId.remove(player.getSessionId());
			}

			// If the player to be removed was ALIVE, set their status to DISCONNECTED to
			// show that they are no longer apart of the lobby, but keep them in the maps so
			// that we can use their player info later.
			if (playerStatus == PlayerStatus.ALIVE) {
				player.setStatus(PlayerStatus.DISCONNECTED);
			}

			log.info("Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Player Id: {}, Player was successfully removed.",
					lobbyId, lobby.getPhase(), lobby.getRoundCount(), player.getId());
			lobbyService.broadcastLobbyState(lobbyId);

			// If the game has already started and too many players have left, go to the not
			// enough players phase.
			if (lobby.getPhase() != LobbyPhase.INTERMISSION && lobby.getPhase() != LobbyPhase.STARTING
					&& lobby.getPhase() != LobbyPhase.ELIMINATION && lobby.getPhase() != LobbyPhase.AI_PLAYER_WON
					&& lobby.getPhase() != LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND
					&& lobby.getPhase() != LobbyPhase.HUMAN_PLAYERS_WON
					&& lobby.getPhase() != LobbyPhase.NOT_ENOUGH_PLAYERS
					&& lobby.getAlivePlayerCount() == END_GAME_EARLY_PLAYER_COUNT) {
				log.info("Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Too many players left after game started.",
						lobbyId, lobby.getPhase(), lobby.getRoundCount(), player.getId());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.NOT_ENOUGH_PLAYERS);
				return player;
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
				return player;
			}

			// If the lobby phase is STARTING and the amount of players is less than 3, go
			// back to intermission.
			if (lobby.getPhase() == LobbyPhase.STARTING && lobby.getPlayerCount() < MIN_PLAYER_COUNT) {
				log.info(
						"Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Too many players left while the game was in the process of starting.",
						lobbyId, lobby.getPhase(), lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.INTERMISSION);
				return player;
			}

			// If a player left during the voting phase and that player was alive, we need
			// to restart the voting phase.
			if (lobby.getPhase() == LobbyPhase.VOTING && playerStatus == PlayerStatus.ALIVE) {
				lobby.setEliminatedPlayerId(playerId);
				log.info(
						"Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Player Id: {}, Alive player left during voting phase, restarting vote phase.",
						lobbyId, lobby.getPhase(), lobby.getRoundCount(), player.getId());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.VOTING_RESTART);
				return player;

			}

			return player;
		}

	}

	/**
	 * The kickPlayer method kicks a player from a given lobby.
	 * 
	 * @param lobbyId  the lobbyId that the player is in
	 * @param playerId the player to kick
	 */
	public void kickPlayer(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			Player player = removePlayer(lobbyId, playerId);
			messagingTemplate.convertAndSend("/queue/lobbies/" + lobbyId + "/disconnect/" + player.getSessionId());
			log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Player Id: {}, Player has been kicked.", lobbyId,
					lobby.getRoundCount(), lobby.getPhase(), playerId);
		}

	}

	/**
	 * The setPlayerAsSpectator method sets a player to a spectator, and is used
	 * after a player is voted out.
	 * 
	 * @param lobbyId  the lobbyId that the player is in
	 * @param playerId the player to set to spectator
	 */
	public void setPlayerAsSpectator(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			Player player = getPlayerById(lobbyId, playerId);
			player.setAvatar(PlayerAvatar.SPECTATOR);
			player.setName(PlayerAvatar.SPECTATOR.getName());
			player.setStatus(PlayerStatus.SPECTATOR);
			lobbyService.broadcastLobbyState(lobbyId);
			log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Player Id: {}, Player has been set as spectator.",
					lobbyId, lobby.getRoundCount(), lobby.getPhase(), playerId);
		}
	}

	/**
	 * The assignPlayerIdentities method assigns a random identity to each player
	 * and sets their status to ALIVE. This method is called the moment the game
	 * starts.
	 * 
	 * @param lobbyId the lobbyId to assign of the players of
	 */
	public void assignPlayerIdentities(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {

			List<UUID> playerIds = lobby.getPlayerIds();

			Collections.shuffle(playerIds);

			// Filter out the HIDDEN and SPECTATOR avatars since they are not avatars for
			// when players are ALIVE.
			List<PlayerAvatar> validAvatars = new ArrayList<>(Arrays.stream(PlayerAvatar.values()).filter(
					(playerAvatar) -> playerAvatar != PlayerAvatar.HIDDEN && playerAvatar != PlayerAvatar.SPECTATOR)
					.toList());

			Collections.shuffle(validAvatars);
			// This map is used for voting, and it serves for quick lookups.
			// We are basically taking a snapshot of all the player names and putting them
			// here.
			Map<String, UUID> playerIdsByName = lobby.getPlayerIdsByName();

			for (int i = 0; i < playerIds.size(); i++) {

				Player player = getPlayerById(lobbyId, playerIds.get(i));
				player.setAvatar(validAvatars.get(i));
				player.setName(validAvatars.get(i).getName());
				player.setStatus(PlayerStatus.ALIVE);
				playerIdsByName.put(player.getName(), player.getId());
			}

			lobbyService.broadcastLobbyState(lobbyId);
			log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Player identities have been assigned.", lobbyId,
					lobby.getRoundCount(), lobby.getPhase());

		}
	}

	/**
	 * The clearPlayerIdentities method clears all previous identities at the end of
	 * a game.
	 * 
	 * @param lobbyId the lobbyId to clear the player identities of
	 */
	public void clearPlayerIdentities(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {

			// Filter out disconnected players, as they will be removed later.
			List<UUID> playerIds = lobby.getPlayersById().entrySet().stream()
					.filter((entry) -> entry.getValue().getStatus() != PlayerStatus.DISCONNECTED)
					.map((entry) -> entry.getKey()).toList();

			// Now we can clear the map of playerIds by name, and this is because
			// its only used for quick lookups when voting.
			lobby.getPlayerIdsByName().clear();

			for (UUID playerId : playerIds) {
				Player player = getPlayerById(lobbyId, playerId);
				player.setName(PlayerAvatar.HIDDEN.getName());
				player.setAvatar(PlayerAvatar.HIDDEN);
				player.setStatus(PlayerStatus.HIDDEN);

			}

			lobbyService.broadcastLobbyState(lobbyId);
			log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Player identities have been cleared.", lobbyId,
					lobby.getRoundCount(), lobby.getPhase());

		}
	}

	/**
	 * The removeDisconnectedPlayers method removes all disconnected players, and is
	 * used at the end of a match.
	 * 
	 * @param lobbyId the lobbyId to remove the disconnected players from
	 */
	public void removeDisconnectedPlayers(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {

			List<UUID> playerIds = lobby.getPlayerIds();
			Map<UUID, Player> playersById = lobby.getPlayersById();
			Map<String, UUID> playerIdsBySessionId = lobby.getPlayerIdsBySessionId();

			List<String> disconnectedSessionIds = playerIdsBySessionId.keySet().stream().filter((sessionId) -> {

				Player player = playersById.get(playerIdsBySessionId.get(sessionId));
				return player.getStatus() == PlayerStatus.DISCONNECTED;

			}).toList();

			disconnectedSessionIds.stream().forEach((sessionId) -> {

				UUID removedPlayerId = playerIdsBySessionId.remove(sessionId);
				playerIds.remove(removedPlayerId);
				playersById.remove(removedPlayerId);

			});
			log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Disconnected players have been removed.", lobbyId,
					lobby.getRoundCount(), lobby.getPhase());
		}
	}

	/**
	 * The playerDisconnectListener is a method that listens to player disconnects,
	 * for example if a player closes a tab or refreshes, that player has basically
	 * left that lobby and we need to remove them from that lobby.
	 * 
	 * @param sessionDisconnectEvent the sessionDisconnectEvent that was sent from
	 *                               the disconnect
	 */
	@EventListener
	public void playerDisconnectListener(SessionDisconnectEvent sessionDisconnectEvent) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(sessionDisconnectEvent.getMessage());
		String sessionId = accessor.getSessionId();
		// Since we do not exactly know why the disconnect event happened, we need if
		// statements to prevent expected exceptions from occuring. A player closing
		// their tab or refreshing will disconnect and then we will need to remove them
		// from the lobby. Compare this to a player clicking leave lobby or if they get
		// kicked, they will already be removed when they disconnect from the browser,
		// meaning we will trigger an exception since removePlayer expects them to still
		// be in the lobby.
		if (lobbyIdByPlayerSessionId.containsKey(sessionId)) {
			Lobby lobby = lobbyService.getLobbyById(lobbyIdByPlayerSessionId.get(sessionId));
			synchronized (lobby) {

				if (lobby.getPlayerIdsBySessionId().containsKey(sessionId)) {
					removePlayer(lobby.getId(), getPlayerIdBySessionId(lobby.getId(), sessionId));
					log.info(
							"Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Player Id: {}, Player has been removed as a result of a disconnect.",
							lobby.getId(), lobby.getRoundCount(), lobby.getPhase(),
							getPlayerIdBySessionId(lobby.getId(), sessionId));
				}
			}
		}
	}

}
