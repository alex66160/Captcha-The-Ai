package com.captchatheai.backend.entity;

import com.captchatheai.backend.entity.avatar.AvatarAccessory;
import com.captchatheai.backend.entity.avatar.AvatarColor;
import com.captchatheai.backend.entity.avatar.AvatarFace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The player entity class.
 * 
 * @author Alex Liu
 */
@Entity
@Table(name = "players")
public class Player {

	/** Id of the player */
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	@Column (name = "player_id", nullable = false)
	private long playerId;
	
	/** Name of the player. */
	@Column (name = "player_name", nullable = false)
	private String playerName = "Placeholder";
	
	/** Avatar accessory of the player */
	@Enumerated(EnumType.STRING)
	@Column (name = "avatar_accessory", nullable = false)
	private AvatarAccessory avatarAccessory;
	
	/** Avatar color of the player */
	@Enumerated(EnumType.STRING)
	@Column (name = "avatar_color", nullable = false)
	private AvatarColor avatarColor;
	
	/** Avatar face of the player */
	@Enumerated(EnumType.STRING)
	@Column (name = "avatar_face", nullable = false)
	private AvatarFace avatarFace;
	
	/** Whether or not player is an ai */
	@Column (name = "is_ai", nullable = false)
	private boolean isAi;
	
	/** Whether or not player is a betrayer */
	@Column (name = "is_betrayer", nullable = false)
	private boolean isBetrayer;
	
	/** Whether or not player is still alive */
	@Column (name = "is_alive", nullable = false)
	private boolean isAlive;
	
	/** Whether or not the player has voted */
	@Column (name = "is_alive", nullable = false)
	private boolean hasVoted;
	
	/** Lobby that the player is assigned to */
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "lobby_id", nullable = false)
	private Lobby lobby;

	/**
	 * @return the playerId
	 */
	public long getPlayerId() {
		return playerId;
	}

	/**
	 * @param playerId the playerId to set
	 */
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @return the playerName
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * @param playerName the playerName to set
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * @return the avatarAccessory
	 */
	public AvatarAccessory getAvatarAccessory() {
		return avatarAccessory;
	}

	/**
	 * @param avatarAccessory the avatarAccessory to set
	 */
	public void setAvatarAccessory(AvatarAccessory avatarAccessory) {
		this.avatarAccessory = avatarAccessory;
	}

	/**
	 * @return the avatarColor
	 */
	public AvatarColor getAvatarColor() {
		return avatarColor;
	}

	/**
	 * @param avatarColor the avatarColor to set
	 */
	public void setAvatarColor(AvatarColor avatarColor) {
		this.avatarColor = avatarColor;
	}

	/**
	 * @return the avatarFace
	 */
	public AvatarFace getAvatarFace() {
		return avatarFace;
	}

	/**
	 * @param avatarFace the avatarFace to set
	 */
	public void setAvatarFace(AvatarFace avatarFace) {
		this.avatarFace = avatarFace;
	}

	/**
	 * @return the isAi
	 */
	public boolean isAi() {
		return isAi;
	}

	/**
	 * @param isAi the isAi to set
	 */
	public void setAi(boolean isAi) {
		this.isAi = isAi;
	}

	/**
	 * @return the isBetrayer
	 */
	public boolean isBetrayer() {
		return isBetrayer;
	}

	/**
	 * @param isBetrayer the isBetrayer to set
	 */
	public void setBetrayer(boolean isBetrayer) {
		this.isBetrayer = isBetrayer;
	}

	/**
	 * @return the isAlive
	 */
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * @param isAlive the isAlive to set
	 */
	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
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
	 * @return the hasVoted
	 */
	public boolean isHasVoted() {
		return hasVoted;
	}

	/**
	 * @param hasVoted the hasVoted to set
	 */
	public void setHasVoted(boolean hasVoted) {
		this.hasVoted = hasVoted;
	}

	/**
	 *
	 * @param playerId
	 * @param playerName
	 * @param avatarAccessory
	 * @param avatarColor
	 * @param avatarFace
	 * @param isAi
	 * @param isBetrayer
	 * @param isAlive
	 * @param hasVoted
	 * @param lobby
	 */
	public Player(long playerId, String playerName, AvatarAccessory avatarAccessory, AvatarColor avatarColor,
			AvatarFace avatarFace, boolean isAi, boolean isBetrayer, boolean isAlive, boolean hasVoted, Lobby lobby) {
		super();
		this.playerId = playerId;
		this.playerName = playerName;
		this.avatarAccessory = avatarAccessory;
		this.avatarColor = avatarColor;
		this.avatarFace = avatarFace;
		this.isAi = isAi;
		this.isBetrayer = isBetrayer;
		this.isAlive = isAlive;
		this.hasVoted = hasVoted;
		this.lobby = lobby;
	}

	/**
	 * No args constructor for player.
	 */
	public Player() {
		super();
	}


	
}
