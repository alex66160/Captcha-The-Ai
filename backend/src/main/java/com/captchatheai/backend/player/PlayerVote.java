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
 * The PlayerVote entity class, keeps track of the number of votes
 * on the players.
 * 
 * @author Alex Liu
 */
@Entity
@Table(name = "player_votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerVote {

	/** Id for the playerVote */
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	@Column(name = "player_vote_id", nullable = false)
	private long playerVoteId;
	
	/** Lobby that the vote was sent from */
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "lobby_id", nullable = false)
	private Lobby lobby;
	
	/** Round number that the playerVote is from */
	@Column (name = "round_number", nullable = false)
	private int roundNumber;
	
	/** Player that sent the vote */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "vote_sender", nullable = false)
	private Player voteSender;
	
	/** Player that got the vote */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "vote_target", nullable = false)
	private Player voteTarget;

	
	
}
