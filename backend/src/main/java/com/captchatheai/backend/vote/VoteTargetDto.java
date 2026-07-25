package com.captchatheai.backend.vote;

import java.util.List;

import com.captchatheai.backend.player.PlayerAvatar;

public record VoteTargetDto (String playerName, PlayerAvatar playerAvatar, List<VoterDto> votersDto){

}
