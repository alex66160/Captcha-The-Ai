package com.captchatheai.backend.vote;

/**
 * The SubmittedVoteEvent record represents an event where a vote was submitted
 * to a lobby.
 * 
 * @author Alex Liu
 * @param lobbyId the lobby that the vote was sent to
 */
public record SubmittedVoteEvent(int lobbyId) {

}
