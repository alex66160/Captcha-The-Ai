package com.captchatheai.backend.player;

/**
 * The EliminatedPlayerResponse record represents a player that has been
 * eliminated from the round, either due to being voted out, disconnecting
 * during the question phase as the question writer, forgetting to write a
 * question as a question writer, or leaving during the voting phase while being
 * alive.
 * 
 * @author Alex Liu
 * @param playerName   the name of the player that was eliminated
 * @param playerAvatar the avatar of the player that was eliminated
 * @param isAi         whether or not the eliminated player was the ai player
 */
public record EliminatedPlayerResponse(String playerName, PlayerAvatar playerAvatar, boolean isAi) {

}
