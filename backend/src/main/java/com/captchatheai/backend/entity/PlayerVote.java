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
 * The PlayerVote entity class, keeps track of the number of votes
 * on the players.
 * 
 * @author Alex Liu
 */
@Entity
@Table(name = "player_votes")
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

	/**
	 * @return the playerVoteId
	 */
	public long getPlayerVoteId() {
		return playerVoteId;
	}

	/**
	 * @param playerVoteId the playerVoteId to set
	 */
	public void setPlayerVoteId(long playerVoteId) {
		this.playerVoteId = playerVoteId;
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
	 * @return the roundNumber
	 */
	public int getRoundNumber() {
		return roundNumber;
	}

	/**
	 * @param roundNumber the roundNumber to set
	 */
	public void setRoundNumber(int roundNumber) {
		this.roundNumber = roundNumber;
	}

	/**
	 * @return the voteSender
	 */
	public Player getVoteSender() {
		return voteSender;
	}

	/**
	 * @param voteSender the voteSender to set
	 */
	public void setVoteSender(Player voteSender) {
		this.voteSender = voteSender;
	}

	/**
	 * @return the voteTarget
	 */
	public Player getVoteTarget() {
		return voteTarget;
	}

	/**
	 * @param voteTarget the voteTarget to set
	 */
	public void setVoteTarget(Player voteTarget) {
		this.voteTarget = voteTarget;
	}

	/**
	 * @param playerVoteId
	 * @param lobby
	 * @param roundNumber
	 * @param voteSender
	 * @param voteTarget
	 */
	public PlayerVote(long playerVoteId, Lobby lobby, int roundNumber, Player voteSender, Player voteTarget) {
		super();
		this.playerVoteId = playerVoteId;
		this.lobby = lobby;
		this.roundNumber = roundNumber;
		this.voteSender = voteSender;
		this.voteTarget = voteTarget;
	}

	/**
	 * No args constructor for playervote.
	 */
	public PlayerVote() {
		super();
	}

	
}
