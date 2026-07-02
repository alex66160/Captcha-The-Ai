package com.captchatheai.backend.lobby;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Lobby entity class.
 * 
 * @author Alex Liu
 */
@Entity
@Table (name = "lobbies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lobby {

	/** Lobby id of the lobby, shown using a 6 digit alphanumeric code */
	@Id
	@Column(name = "lobby_id", nullable = false, length = 6)
	private String lobbyId;
	
	/** Lobby name for the lobby */
	@Column(name = "lobby_name", nullable = false, length = 30)
	private String lobbyName;
	
	/** Current round of the lobby */
	@Column(name = "current_round", nullable = false)
	private int currentRound;
	
	/** Max rounds for the lobby */
	@Column(name = "max_rounds", nullable = false)
	private int maxRounds;
	
	/** Max vote skips for the lobby */
	@Column(name = "max_vote_skips", nullable = false)
	private int maxVoteSkips;
	
	/** Game phase for the lobby */
	@Enumerated(EnumType.STRING)
	@Column(name = "game_phase", nullable = false)
	private GamePhase gamePhase;
	
	/** Current timer of the lobby */
	@Column(name = "timer", nullable = false)
	private int timer;
	
	/** max players of the lobby */
	@Column(name = "max_players", nullable = false)
	private int maxPlayers;
	
	/** Password for the lobby, optional. */
	@Column(name = "password", nullable = true)
	private String password;
	
	/** time for the question phase */
	@Column(name = "time_question_phase", nullable = false)
	private int timeQuestionPhase;
	
	/** time for the answer phase */
	@Column(name = "time_answer_phase", nullable = false)
	private int timeAnswerPhase;
	
	/** time for the vote phase */
	@Column(name = "time_vote_phase", nullable = false)
	private int timeVotePhase;

	

	
	
	
}
