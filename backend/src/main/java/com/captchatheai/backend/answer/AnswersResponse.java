package com.captchatheai.backend.answer;

import java.util.List;

/**
 * The AnswersResponse represents all the answers in a lobby.
 * 
 * @author Alex Liu
 * @param answers the answers for all the players in a lobby
 * 
 */
public record AnswersResponse(List<AnswerResponse> answers) {

}
