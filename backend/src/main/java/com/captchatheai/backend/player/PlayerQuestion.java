package com.captchatheai.backend.player;

import com.captchatheai.backend.lobby.Lobby;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The PlayerQuestion entity class, keeps track of the questions asked in a lobby.
 * 
 * @author Alex Liu
 */
@Entity
@Table(name = "player_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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


	
	
}
