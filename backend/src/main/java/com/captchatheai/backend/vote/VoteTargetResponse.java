package com.captchatheai.backend.vote;

import java.util.List;

import com.captchatheai.backend.player.PlayerAvatar;

/**
 * The VoteTargetResponse record represents a player and the list of players who
 * voted for that player.
 * 
 * @author Alex Liu
 * @param playerName   the player that was voted on
 * @param playerAvatar the avatar of the player that was voted on
 * @param voters       the list of voters that voted for the player
 */
public record VoteTargetResponse(String playerName, PlayerAvatar playerAvatar, List<VoterResponse> voters) {

}
