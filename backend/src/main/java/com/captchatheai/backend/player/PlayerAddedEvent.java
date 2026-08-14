package com.captchatheai.backend.player;

/**
 * The PlayerAddedEvent record represents that a player has been added to a
 * lobby.
 * 
 * @author Alex Liu
 * @param lobbyId the lobby that had the added player
 */
public record PlayerAddedEvent(int lobbyId) {

}
