package com.captchatheai.backend.player;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyLookup;
import com.captchatheai.backend.player.exception.PlayerNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * The PlayerLookup class contains lookup method such as returning player by id,
 * returning playerId by name, and playerId by sessionid.
 * 
 * @author Alex Liu
 */
@Service
@RequiredArgsConstructor
public class PlayerLookup {

	private final LobbyLookup lobbyLookup;

	/**
	 * The getPlayerById method gets a player using their playerId. Method was
	 * originally from playerService but was extracted to PlayerLookup to remove
	 * circular dependencies.
	 * 
	 * @param lobbyId  the lobbyId that the player belongs to
	 * @param playerId the playerId of the player we want to get
	 * @return the player found from the playerid
	 * @throws PlayerNotFoundException if that playerId doesnt exist in the lobbyId
	 *                                 given
	 */
	public Player getPlayerById(int lobbyId, UUID playerId) {
		Player player = lobbyLookup.getLobbyById(lobbyId).getPlayersById().get(playerId);
		if (player == null) {
			throw new PlayerNotFoundException(
					"Lobby Id: " + lobbyId + ", Player Id: " + playerId + ", Player Id was not found.");
		}
		return player;

	}

	/**
	 * The getPlayerIdByName method gets a playerId using their name. Method was
	 * originally from playerService but was extracted to PlayerLookup to remove
	 * circular dependencies.
	 * 
	 * @param lobbyId    the lobbyId that the player belongs to
	 * @param playerName the playerName of the playerId we want to get
	 * @return the playerId found by the player name
	 * @throws PlayerNotFoundException if that playerName doesnt exist in the
	 *                                 lobbyId given
	 */
	public UUID getPlayerIdByName(int lobbyId, String playerName) {
		UUID playerId = lobbyLookup.getLobbyById(lobbyId).getPlayerIdsByName().get(playerName);
		if (playerId == null) {
			throw new PlayerNotFoundException(
					"Lobby Id: " + lobbyId + ", Player Name: " + playerName + ", Player name was not found.");
		}
		return playerId;
	}

	/**
	 * The getPlayerBySessionId method gets a playerId using their sessionId. Method
	 * was originally from playerService but was extracted to PlayerLookup to remove
	 * circular dependencies.
	 * 
	 * @param lobbyId   the lobbyId that the player belongs to
	 * @param sessionId the sessionId of the playerId we want to get
	 * @return the playerId found by the session id
	 * @throws PlayerNotFoundException if that sessionId doesnt exist in the lobbyId
	 *                                 given
	 */
	public UUID getPlayerIdBySessionId(int lobbyId, String sessionId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
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
}
