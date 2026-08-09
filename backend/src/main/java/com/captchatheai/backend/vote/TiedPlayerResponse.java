package com.captchatheai.backend.vote;

import com.captchatheai.backend.player.PlayerAvatar;

/**
 * The TiedPlayerResponse record represents a tied player after votes are
 * revealed.
 * 
 * @author Alex Liu
 * @param playerName   the name of the tied player
 * @param playerAvatar the avatar of the tied player
 * @param isVotedOut   whether or not thats the player that will be voted out
 */
public record TiedPlayerResponse(String playerName, PlayerAvatar playerAvatar, boolean isVotedOut) {

}
