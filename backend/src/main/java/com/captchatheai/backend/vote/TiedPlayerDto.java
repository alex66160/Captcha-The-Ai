package com.captchatheai.backend.vote;

import com.captchatheai.backend.player.PlayerAvatar;

public record TiedPlayerDto (String playerName, PlayerAvatar playerAvatar, boolean isVotedOut){

}
