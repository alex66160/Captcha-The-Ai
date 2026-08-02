package com.captchatheai.backend.answer;

import java.util.List;

/**
 * The AnswersDto record is a data transfer object to represent all the answers
 * in a lobby.
 * 
 * @author Alex Liu
 */
public record AnswersDto(List<AnswerDto> answers) {

}
