package com.captchatheai.backend.vote;

import com.captchatheai.backend.player.PlayerAvatar;

/**
 * The VoterResponse record represents a singular player that voted.
 * 
 * @author Alex Liu
 * @param playerName   the name of the voter
 * @param playerAvatar the avatar of the voter
 */
public record VoterResponse(String playerName, PlayerAvatar playerAvatar) {

}
