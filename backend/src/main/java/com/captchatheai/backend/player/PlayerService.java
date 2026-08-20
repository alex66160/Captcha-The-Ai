package com.captchatheai.backend.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyLookup;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.player.exception.GetAiPlayerDeniedException;
import com.captchatheai.backend.player.exception.PlayerDisconnectedException;

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
	private final LobbyLookup lobbyLookup;

	private final PlayerLookup playerLookup;

	private final ApplicationEventPublisher eventPublisher;

	private final SimpMessagingTemplate messagingTemplate;

	/**
	 * Represents the minimum amount of players to start the game.
	 */
	public final static int MIN_PLAYER_COUNT = 3;

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
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			Player eliminatedPlayer = playerLookup.getPlayerById(lobbyId, lobby.getEliminatedPlayerId());
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
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			if (lobby.getPhase() != LobbyPhase.AI_PLAYER_WON
					&& lobby.getPhase() != LobbyPhase.AI_PLAYER_FAILED_TO_RESPOND
					&& lobby.getPhase() != LobbyPhase.HUMAN_PLAYERS_WON
					&& lobby.getPhase() != LobbyPhase.NOT_ENOUGH_PLAYERS) {
				throw new GetAiPlayerDeniedException(
						"Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount() + ", Lobby Phase: "
								+ lobby.getPhase() + ", Get Ai Player Denied: Lobby is not in a game result phase.");
			}

			Player aiPlayer = playerLookup.getPlayerById(lobbyId, lobby.getAiPlayerId());
			return new AiPlayerResponse(aiPlayer.getName(), aiPlayer.getAvatar());
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
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			// Filter all disconnected players from the list as they are not meant to be
			// shown.
			List<UUID> playerIds = lobby
					.getPlayerIds().stream().filter((playerIdFromLobby) -> playerLookup
							.getPlayerById(lobbyId, playerIdFromLobby).getStatus() != PlayerStatus.DISCONNECTED)
					.toList();

			// Make the list of player states by mapping each player to a PlayerState
			// record.
			List<PlayerState> players = playerIds.stream().map((playerIdFromPlayerIds) -> {
				Player player = playerLookup.getPlayerById(lobbyId, playerIdFromPlayerIds);
				return new PlayerState(player.getName(), player.getAvatar(),
						playerId.equals(lobby.getQuestionWriterId()), player.getId().equals(playerId));

			}).toList();

			return players;
		}
	}

	/**
	 * The addPlayer method adds a player to a given lobby.
	 * 
	 * @param lobbyId   the lobbyId to add to
	 * @param sessionId the sessionId of the player we want to add.
	 * @return the player that was added
	 */
	public Player addPlayer(int lobbyId, String sessionId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
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

			eventPublisher.publishEvent(new PlayerAddedEvent(lobbyId));

			return player;
		}
	}

	/**
	 * The removePlayer method removes a player from a lobby.
	 * 
	 * @param lobbyId  the lobbyId to remove the player from
	 * @param playerId the player to be removed
	 * @throws PlayerDisconnectedException if player to remove was already
	 *                                     disconnected
	 */
	public void removePlayer(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			Player player = playerLookup.getPlayerById(lobbyId, playerId);
			PlayerStatus initialPlayerStatus = player.getStatus();

			if (initialPlayerStatus == PlayerStatus.DISCONNECTED) {
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
			if (initialPlayerStatus == PlayerStatus.HIDDEN || initialPlayerStatus == PlayerStatus.SPECTATOR) {
				playerIds.remove(playerId);
				playersById.remove(playerId);
				playerIdsBySessionId.remove(player.getSessionId());
			}

			// If the player to be removed was ALIVE, set their status to DISCONNECTED to
			// show that they are no longer apart of the lobby, but keep them in the maps so
			// that we can use their player info later.
			if (initialPlayerStatus == PlayerStatus.ALIVE) {
				player.setStatus(PlayerStatus.DISCONNECTED);
			}

			log.info("Lobby Id: {}, Lobby Phase: {}, Lobby Round: {}, Player Id: {}, Player was successfully removed.",
					lobbyId, lobby.getPhase(), lobby.getRoundCount(), player.getId());

			eventPublisher.publishEvent(new PlayerRemovedEvent(lobbyId, playerId, initialPlayerStatus));

		}

	}

	/**
	 * The kickPlayer method kicks a player from a given lobby.
	 * 
	 * @param lobbyId  the lobbyId that the player is in
	 * @param playerId the player to kick
	 */
	public void kickPlayer(int lobbyId, UUID playerId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			String sessionId = playerLookup.getPlayerById(lobbyId, playerId).getSessionId();
			removePlayer(lobbyId, playerId);
			messagingTemplate.convertAndSend("/queue/lobbies/" + lobbyId + "/disconnect/" + sessionId);
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
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {
			Player player = playerLookup.getPlayerById(lobbyId, playerId);
			player.setAvatar(PlayerAvatar.SPECTATOR);
			player.setName(PlayerAvatar.SPECTATOR.getName());
			player.setStatus(PlayerStatus.SPECTATOR);
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
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
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

				Player player = playerLookup.getPlayerById(lobbyId, playerIds.get(i));
				player.setAvatar(validAvatars.get(i));
				player.setName(validAvatars.get(i).getName());
				player.setStatus(PlayerStatus.ALIVE);
				playerIdsByName.put(player.getName(), player.getId());
			}

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
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		synchronized (lobby) {

			// Filter out disconnected players, as they will be removed later.
			List<UUID> playerIds = lobby.getPlayersById().entrySet().stream()
					.filter((entry) -> entry.getValue().getStatus() != PlayerStatus.DISCONNECTED)
					.map((entry) -> entry.getKey()).toList();

			// Now we can clear the map of playerIds by name, and this is because
			// its only used for quick lookups when voting.
			lobby.getPlayerIdsByName().clear();

			for (UUID playerId : playerIds) {
				Player player = playerLookup.getPlayerById(lobbyId, playerId);
				player.setName(PlayerAvatar.HIDDEN.getName());
				player.setAvatar(PlayerAvatar.HIDDEN);
				player.setStatus(PlayerStatus.HIDDEN);

			}

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
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
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
	 * The handleSessionDisconnectEvent is a method that listens to player
	 * disconnects, for example if a player closes a tab or refreshes, that player
	 * has basically left that lobby and we need to remove them from that lobby.
	 * 
	 * @param sessionDisconnectEvent the sessionDisconnectEvent that was sent from
	 *                               the disconnect
	 */
	@EventListener
	public void handleSessionDisconnectEvent(SessionDisconnectEvent sessionDisconnectEvent) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(sessionDisconnectEvent.getMessage());
		String sessionId = accessor.getSessionId();
		log.info("ASDFASDFASDF sessionId: {}", sessionId);
		// Since we do not exactly know why the disconnect event happened, we need if
		// statements to prevent expected exceptions from occuring. A player closing
		// their tab or refreshing will disconnect and then we will need to remove them
		// from the lobby. Compare this to a player clicking leave lobby or if they get
		// kicked, they will already be removed when they disconnect from the browser,
		// meaning we will trigger an exception since removePlayer expects them to still
		// be in the lobby.
		if (lobbyIdByPlayerSessionId.containsKey(sessionId)) {
			Lobby lobby = lobbyLookup.getLobbyById(lobbyIdByPlayerSessionId.get(sessionId));
			synchronized (lobby) {

				if (lobby.getPlayerIdsBySessionId().containsKey(sessionId)) {
					removePlayer(lobby.getId(), playerLookup.getPlayerIdBySessionId(lobby.getId(), sessionId));
//					log.info(
//							"Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Player Id: {}, Player has been removed as a result of a disconnect.",
//							lobby.getId(), lobby.getRoundCount(), lobby.getPhase(),
//							playerLookup.getPlayerIdBySessionId(lobby.getId(), sessionId));
				}
			}
		}
	}

}
