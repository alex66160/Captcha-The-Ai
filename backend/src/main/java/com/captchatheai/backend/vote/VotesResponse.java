package com.captchatheai.backend.vote;

import java.util.List;

/**
 * The VotesResponse record represents a list of vote targets, and each vote
 * target includes a list of players that voted for the vote target.
 * 
 * If confused, check out the VoteTargetResponse record for clarification, as
 * VotesResponse is a wrapper for the voteTargets list.
 * 
 * @author Alex Liu
 * @param voteTargets the list of voteTargets
 */
public record VotesResponse(List<VoteTargetResponse> voteTargets) {

}
