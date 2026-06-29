package com.captchatheai.backend.entity;

import java.time.LocalDateTime;

import com.captchatheai.backend.entity.lobby.GamePhase;
import com.captchatheai.backend.entity.lobby.ModeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The Lobby entity class.
 * 
 * @author Alex Liu
 */
@Entity
@Table (name = "lobbies")
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
	
	/** Mode type for the lobby */
	@Enumerated(EnumType.STRING)
	@Column(name = "mode_type", nullable = false)
	private ModeType modeType;
	
	/** Game phase for the lobby */
	@Enumerated(EnumType.STRING)
	@Column(name = "game_phase", nullable = false)
	private GamePhase gamePhase;
	
	/** Current timer of the lobby */
	@Column(name = "timer", nullable = false)
	private LocalDateTime timer;
	
	/** min players of the lobby */
	@Column(name = "min_players", nullable = false)
	private int minPlayers;
	
	/** max players of the lobby */
	@Column(name = "max_players", nullable = false)
	private int maxPlayers;

	/**
	 * @return the lobbyId
	 */
	public String getLobbyId() {
		return lobbyId;
	}

	/**
	 * @param lobbyId the lobbyId to set
	 */
	public void setLobbyId(String lobbyId) {
		this.lobbyId = lobbyId;
	}

	/**
	 * @return the lobbyName
	 */
	public String getLobbyName() {
		return lobbyName;
	}

	/**
	 * @param lobbyName the lobbyName to set
	 */
	public void setLobbyName(String lobbyName) {
		this.lobbyName = lobbyName;
	}

	/**
	 * @return the currentRound
	 */
	public int getCurrentRound() {
		return currentRound;
	}

	/**
	 * @param currentRound the currentRound to set
	 */
	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}

	/**
	 * @return the maxRounds
	 */
	public int getMaxRounds() {
		return maxRounds;
	}

	/**
	 * @param maxRounds the maxRounds to set
	 */
	public void setMaxRounds(int maxRounds) {
		this.maxRounds = maxRounds;
	}

	/**
	 * @return the maxVoteSkips
	 */
	public int getMaxVoteSkips() {
		return maxVoteSkips;
	}

	/**
	 * @param maxVoteSkips the maxVoteSkips to set
	 */
	public void setMaxVoteSkips(int maxVoteSkips) {
		this.maxVoteSkips = maxVoteSkips;
	}

	/**
	 * @return the modeType
	 */
	public ModeType getModeType() {
		return modeType;
	}

	/**
	 * @param modeType the modeType to set
	 */
	public void setModeType(ModeType modeType) {
		this.modeType = modeType;
	}

	/**
	 * @return the gamePhase
	 */
	public GamePhase getGamePhase() {
		return gamePhase;
	}

	/**
	 * @param gamePhase the gamePhase to set
	 */
	public void setGamePhase(GamePhase gamePhase) {
		this.gamePhase = gamePhase;
	}

	/**
	 * @return the timer
	 */
	public LocalDateTime getTimer() {
		return timer;
	}

	/**
	 * @param timer the timer to set
	 */
	public void setTimer(LocalDateTime timer) {
		this.timer = timer;
	}

	/**
	 * @return the minPlayers
	 */
	public int getMinPlayers() {
		return minPlayers;
	}

	/**
	 * @param minPlayers the minPlayers to set
	 */
	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	/**
	 * @return the maxPlayers
	 */
	public int getMaxPlayers() {
		return maxPlayers;
	}

	/**
	 * @param maxPlayers the maxPlayers to set
	 */
	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	/**
	 * No args constructor for lobby
	 */
	public Lobby() {
		super();
	}
	
	
}
