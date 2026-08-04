package com.captchatheai.backend.answer;

import com.captchatheai.backend.player.PlayerAvatar;

/**
 * The AnswerResponseDto record represents an answer for a player.
 * 
 * @param playerName   the name of the player
 * @param playerAvatar the avatar of the player
 * @param answer       the answer of the player
 * 
 * @author Alex Liu
 */
public record AnswerResponseDto(String playerName, PlayerAvatar playerAvatar, String answer) {

}
