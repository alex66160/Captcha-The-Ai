package com.captchatheai.backend.question;

import com.captchatheai.backend.player.PlayerAvatar;

/**
 * The QuestionResponse record represents the question from the question writer.
 * 
 * @author Alex Liu
 * @param playerName   the name of the player that wrote the question
 * @param playerAvatar the avatar of the player that wrote the question
 * @param question     the question that was written
 */
public record QuestionResponse(String playerName, PlayerAvatar playerAvatar, String question) {

}
