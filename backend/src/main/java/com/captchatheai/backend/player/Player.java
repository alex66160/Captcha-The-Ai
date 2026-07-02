package com.captchatheai.backend.player;

import com.captchatheai.backend.lobby.Lobby;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The player entity class.
 * 
 * @author Alex Liu
 */
@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {

	/** Id of the player */
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	@Column (name = "player_id", nullable = false)
	private Long playerId;
	
	/** Name of the player. */
	@Column (name = "player_name", nullable = false)
	private String playerName = "Placeholder";
	
	/** Avatar color of the player */
	@Enumerated(EnumType.STRING)
	@Column (name = "avatar_color", nullable = false)
	private AvatarColor avatarColor;
	
	/** Avatar eyes of the player */
	@Enumerated(EnumType.STRING)
	@Column (name = "avatar_eyes", nullable = false)
	private AvatarEyes avatarEyes;
	
	/** Avatar mouth of the player */
	@Enumerated(EnumType.STRING)
	@Column (name = "avatar_mouth", nullable = false)
	private AvatarMouth avatarMouth;
	
	/** Whether or not player is an ai */
	@Column (name = "is_ai", nullable = false)
	private boolean isAi;
	
	/** Whether or not player is still alive */
	@Column (name = "is_alive", nullable = false)
	private boolean isAlive;
	
	/** Whether or not the player has voted */
	@Column (name = "has_voted", nullable = false)
	private boolean hasVoted;
	
	/** Whether or not the player is an admin of the lobby
	 *  (By Admin, as in can control the lobby settings) */
	@Column (name = "is_admin", nullable = false)
	private boolean isAdmin;
	
	/** Lobby that the player is assigned to */
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "lobby_id", nullable = false)
	private Lobby lobby;

	



	
}
