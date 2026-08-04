package com.captchatheai.backend.answer;

import java.util.List;

/**
 * The AnswersResponseDto represents all the answers in a lobby.
 * 
 * @param answers the answers for all the players in a lobby
 * 
 * @author Alex Liu
 */
public record AnswersResponseDto(List<AnswerResponseDto> answers) {

}
