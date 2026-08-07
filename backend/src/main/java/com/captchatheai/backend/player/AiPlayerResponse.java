package com.captchatheai.backend.player;

/**
 * The AiPlayerResponse record represents the ai player in the lobby.
 * 
 * @author Alex Liu
 * @param playerName   the name of the ai player
 * @param playerAvatar the avatar of the ai player
 */
public record AiPlayerResponse(String playerName, PlayerAvatar playerAvatar) {

}
