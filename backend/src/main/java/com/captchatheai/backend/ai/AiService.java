package com.captchatheai.backend.ai;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.captchatheai.backend.answer.AnswerService;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyLookup;
import com.captchatheai.backend.question.QuestionService;
import com.captchatheai.backend.vote.VoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The AiService class is responsible for generating questions, answers, and
 * votes for the ai player.
 * 
 * @author Alex Liu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

	private final LobbyLookup lobbyLookup;

	private final QuestionService questionService;

	private final AnswerService answerService;

	private final VoteService voteService;

	/**
	 * The handleScheduledAiEvent handles an ai event, such as generating a
	 * question, answer, or vote for the ai player.
	 * 
	 * @param lobbyId          the lobby that has the ai event
	 * @param scheduledAiEvent the ai event to be handled
	 */
	@Async
	public void handleScheduledAiEvent(int lobbyId, ScheduledAiEvent scheduledAiEvent) {
		switch (scheduledAiEvent.getAiEvent()) {

		case GENERATE_QUESTION -> generateQuestion(lobbyId);

		case GENERATE_ANSWER -> generateAnswer(lobbyId);

		case GENERATE_VOTE -> generateVote(lobbyId);
		}
	}

	/**
	 * The generateQuestion method will generate a question for the ai player and
	 * call question service to submit it on behalf of the ai player.
	 * 
	 * @param lobbyId the lobby to generate and send the question for the ai player
	 */

	public void generateQuestion(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Began generating question for Ai Player.", lobbyId,
				lobby.getRoundCount(), lobby.getPhase());

		// call llm here
		synchronized (lobby) {
			questionService.sendQuestion(lobbyId, lobby.getAiPlayerId(), "PLACEHOLDER QUESTION FOR AI PLAYER.");
		}
		log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Finished generating question for Ai Player.", lobbyId,
				lobby.getRoundCount(), lobby.getPhase());

	}

	/**
	 * The generateAnswer method will generate an answer for the ai player and call
	 * answer service to submit it on behalf of the ai player.
	 * 
	 * @param lobbyId the lobby to generate and send the answer for the ai player
	 */

	public void generateAnswer(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Began generating answer for Ai Player.", lobbyId,
				lobby.getRoundCount(), lobby.getPhase());

		// call llm here
		synchronized (lobby) {
			answerService.sendAnswer(lobbyId, lobby.getAiPlayerId(), "PLACEHOLDER ANSWER FOR AI PLAYER.");
		}
		log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Finished generating answer for Ai Player.", lobbyId,
				lobby.getRoundCount(), lobby.getPhase());
	}

	/**
	 * The generateVote method will generate a vote for the ai player and call vote
	 * service to submit it on behalf of the ai player.
	 * 
	 * @param lobbyId the lobby to generate and send the vote for the ai player
	 */

	public void generateVote(int lobbyId) {
		Lobby lobby = lobbyLookup.getLobbyById(lobbyId);
		log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Began generating vote for Ai Player.", lobbyId,
				lobby.getRoundCount(), lobby.getPhase());

		// call llm here
		synchronized (lobby) {
			voteService.sendVote(lobbyId, lobby.getAiPlayerId(), lobby.getAiPlayerId());
		}
		log.info("Lobby Id: {}, Lobby Round: {}, Lobby Phase: {}, Finished generating vote for Ai Player.", lobbyId,
				lobby.getRoundCount(), lobby.getPhase());
	}
}
