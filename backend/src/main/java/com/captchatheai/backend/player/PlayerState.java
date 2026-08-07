package com.captchatheai.backend.player;

/**
 * The PlayerState record represents a player in a given lobby.
 * 
 * @author Alex Liu
 * @param playerName       the name of the player
 * @param playerAvatar     the avatar of the player
 * @param isQuestionWriter whether or not the player is the question writer
 * @param isSelf           whether or not the player is themself
 */
public record PlayerState(String playerName, PlayerAvatar playerAvatar, boolean isQuestionWriter, boolean isSelf) {

}
