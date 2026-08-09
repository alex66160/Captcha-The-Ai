package com.captchatheai.backend.answer;

import com.captchatheai.backend.player.PlayerAvatar;

/**
 * The AnswerResponse record represents an answer for a player.
 * 
 * @author Alex Liu
 * @param playerName   the name of the player
 * @param playerAvatar the avatar of the player
 * @param answer       the answer of the player
 * 
 */
public record AnswerResponse(String playerName, PlayerAvatar playerAvatar, String answer) {

}
