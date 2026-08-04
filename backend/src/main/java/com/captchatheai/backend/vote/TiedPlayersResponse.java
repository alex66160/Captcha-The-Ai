package com.captchatheai.backend.vote;

import java.util.List;

/**
 * The TiedPlayersResponse record represents list of tied players after votes
 * are revealed.
 * 
 * @author Alex Liu
 * @param tiedPlayers the list of tied players
 */
public record TiedPlayersResponse(List<TiedPlayerResponse> tiedPlayers) {

}
