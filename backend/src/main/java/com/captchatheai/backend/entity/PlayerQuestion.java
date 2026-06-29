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
 * The PlayerQuestion entity class, keeps track of the questions asked in a lobby.
 * 
 * @author Alex Liu
 */
@Entity
@Table(name = "player_questions")
public class PlayerQuestion {
	
	/** Id for the playerQuestion */
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	@Column(name = "player_question_id", nullable = false)
	private long playerQuestionId;
	
	/** Question for the playerQuestion */
	@Column(name = "question", nullable = false, length = 100)
	private String question;
	
	/** Player that sent the question */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "player_id", nullable = false)
	private Player player;
	
	/** Lobby that the question was sent from */
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "lobby_id", nullable = false)
	private Lobby lobby;
	
	/** Flag to see if the question was sent by an ai */
	@Column (name = "is_ai_generated", nullable = false)
	private boolean isAiGenerated;

	/**
	 * @return the playerQuestionId
	 */
	public long getPlayerQuestionId() {
		return playerQuestionId;
	}

	/**
	 * @param playerQuestionId the playerQuestionId to set
	 */
	public void setPlayerQuestionId(long playerQuestionId) {
		this.playerQuestionId = playerQuestionId;
	}

	/**
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * @param question the question to set
	 */
	public void setQuestion(String question) {
		this.question = question;
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
	 * No args constructor for player question
	 */
	public PlayerQuestion() {
		super();
	}
	
	
}
