package com.captchatheai.backend.answer;

/**
 * The SubmittedAnswerEvent record represents the event where a player has
 * submitted an answer.
 * 
 * @author Alex Liu
 * @param lobbyId the lobby where the answer was submitted
 */
public record SubmittedAnswerEvent(int lobbyId) {

}
