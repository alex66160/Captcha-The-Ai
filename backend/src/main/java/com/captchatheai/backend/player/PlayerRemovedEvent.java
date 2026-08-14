package com.captchatheai.backend.player;

import java.util.UUID;

/**
 * The PlayerRemovedEvent record represents an event where a player was removed
 * from the lobby.
 * 
 * @author Alex Liu
 * @param lobbyId  the lobby the player was removed from
 * @param playerId the player that was removed
 */
public record PlayerRemovedEvent(int lobbyId, UUID playerId, PlayerStatus initialPlayerStatus) {

}
