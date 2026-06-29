package com.captchatheai.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The PlayerAnswer entity class, keeps track of the answers from players in a lobby.
 * 
 * @author Alex Liu
 */
@Entity
@Table(name = "player_answers")
public class PlayerAnswer {
	
	/** Id for the player answer */
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	@Column(name = "player_answer_id", nullable = false)
	private long playerAnswerId;
	
	/** Flag to see if the answer was sent by an ai */
	@Column (name = "is_ai_generated", nullable = false)
	private boolean isAiGenerated;
	
	/** Answer for the playerAnswer */
	@Column(name = "answer", nullable = false, length = 100)
	private String answer;
	
	/** Player that sent the answer */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "player_id", nullable = false)
	private Player player;
	
	/** Lobby that the answer was sent from */
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "lobby_id", nullable = false)
	private Lobby lobby;
	
	/** Question that the answer is for */
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "player_question_id", nullable = false)
	private PlayerQuestion playerQuestion;

	/**
	 * @return the playerAnswerId
	 */
	public long getPlayerAnswerId() {
		return playerAnswerId;
	}

	/**
	 * @param playerAnswerId the playerAnswerId to set
	 */
	public void setPlayerAnswerId(long playerAnswerId) {
		this.playerAnswerId = playerAnswerId;
	}

	/**
	 * @return the isAiGenerated
	 */
	public boolean isAiGenerated() {
		return isAiGenerated;
	}

	/**
	 * @param isAiGenerated the isAiGenerated to set
	 */
	public void setAiGenerated(boolean isAiGenerated) {
		this.isAiGenerated = isAiGenerated;
	}

	/**
	 * @return the answer
	 */
	public String getAnswer() {
		return answer;
	}

	/**
	 * @param answer the answer to set
	 */
	public void setAnswer(String answer) {
		this.answer = answer;
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * @return the lobby
	 */
	public Lobby getLobby() {
		return lobby;
	}

	/**
	 * @param lobby the lobby to set
	 */
	public void setLobby(Lobby lobby) {
		this.lobby = lobby;
	}

	/**
	 * @return the playerQuestion
	 */
	public PlayerQuestion getPlayerQuestion() {
		return playerQuestion;
	}

	/**
	 * @param playerQuestion the playerQuestion to set
	 */
	public void setPlayerQuestion(PlayerQuestion playerQuestion) {
		this.playerQuestion = playerQuestion;
	}

	/**
	 * No args constructor for player answer
	 */
	public PlayerAnswer() {
		super();
	}
	
	
	
}
