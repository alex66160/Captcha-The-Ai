package com.captchatheai.backend.vote;

/**
 * The SubmitVoteRequest record stores a player's vote to target another player.
 * 
 * @author Alex Liu
 * @param voteTargetName the name of the player thats getting voted on
 * 
 */
public record SubmitVoteRequest(String voteTargetName) {

}
