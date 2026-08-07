package com.captchatheai.backend.player;

/**
 * The PlayerStatus enum represents the status of a given player.
 * 
 * @author Alex Liu
 */
public enum PlayerStatus {
	/**
	 * Hidden means that the player is currently in the intermission or starting
	 * phase, waiting for the game to start
	 */
	HIDDEN,
	/**
	 * Alive means that the player is in the lobby and is counted as a participant
	 */
	ALIVE,
	/**
	 * Spectator means that the player was voted out or joined in the middle of a
	 * game
	 */
	SPECTATOR,
	/**
	 * Disconnected means that the player used to be alive, we have this status so
	 * that we can store disconnected players in the lobby
	 */
	DISCONNECTED;
}
