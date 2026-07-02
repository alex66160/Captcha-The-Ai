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
 * The PlayerAnswer entity class, keeps track of the answers from players in a lobby.
 * 
 * @author Alex Liu
 */
@Entity
@Table(name = "player_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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


	
	
	
}
