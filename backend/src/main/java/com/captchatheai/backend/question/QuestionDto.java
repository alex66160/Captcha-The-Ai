package com.captchatheai.backend.question;

import com.captchatheai.backend.player.PlayerAvatar;

public record QuestionDto(String playerName, PlayerAvatar playerAvatar, String question) {

}
