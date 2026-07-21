package com.captchatheai.backend.answer;

import com.captchatheai.backend.player.PlayerAvatar;

public record AnswerDto (String playerName, PlayerAvatar playerAvatar, String answer){

}
