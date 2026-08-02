package com.captchatheai.backend.answer;

import com.captchatheai.backend.player.PlayerAvatar;

/**
 * The AnswerDto record is a data transfer object to represent an answer for a
 * player.
 * 
 * @author Alex Liu
 */
public record AnswerDto(String playerName, PlayerAvatar playerAvatar, String answer) {

}
