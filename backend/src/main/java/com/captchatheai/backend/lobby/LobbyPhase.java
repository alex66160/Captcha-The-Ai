package com.captchatheai.backend.lobby;

/**
 * The LobbyPhase enum class represents all the possible lobby phases for a
 * game, along with their duration.
 * 
 * @author Alex Liu
 */
public enum LobbyPhase {
	INTERMISSION(1200), STARTING(40), INTRO(10), QUESTION_ANNOUNCEMENT(3), QUESTION(20), QUESTION_DISCONNECT(3),
	QUESTION_EMPTY(3), ANSWER_ANNOUNCEMENT(3), ANSWER(20), DISCUSS_ANNOUNCEMENT(3), DISCUSS(20), VOTING(20),
	VOTING_RESTART(3), REVEAL_ANNOUNCEMENT(3), REVEAL(5), REVEAL_TIE(5), ELIMINATION(5), AI_PLAYER_WON(10),
	AI_PLAYER_FAILED_TO_RESPOND(10), HUMAN_PLAYERS_WON(10), NOT_ENOUGH_PLAYERS(10);

	private final int duration;

	private LobbyPhase(int duration) {
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

}
