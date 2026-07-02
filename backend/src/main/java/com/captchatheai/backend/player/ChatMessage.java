package com.captchatheai.backend.player;

import java.time.LocalDateTime;

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

/**
 * The ChatMessage entity class.
 * 
 * @author Alex Liu
 */
@Entity
@Table (name = "chat_messages")
public class ChatMessage {

	/** Id of the chatmessage */
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	@Column (name = "chat_message_id", nullable = false)
	private long chatMessageId;
	
	/** The message for the chatmessage */
	@Column (name = "message", nullable = false, length = 100)
	private String message;
	
	/** Player that sent the message */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "player_id", nullable = false)
	private Player player;
	
	/** Timestamp for when the message was sent */
	@Column (name = "time_stamp", nullable = false)
	private LocalDateTime timeStamp = LocalDateTime.now();
	
	/** Lobby that the message was sent from */
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "lobby_id", nullable = false)
	private Lobby lobby;
	
	/** Flag to see if the message was sent by an ai */
	@Column (name = "is_ai_generated", nullable = false)
	private boolean isAiGenerated;

	/**
	 * @return the chatMessageId
	 */
	public long getChatMessageId() {
		return chatMessageId;
	}

	/**
	 * @param chatMessageId the chatMessageId to set
	 */
	public void setChatMessageId(long chatMessageId) {
		this.chatMessageId = chatMessageId;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
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
	 * @return the timeStamp
	 */
	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(LocalDateTime timeStamp) {
		this.timeStamp = timeStamp;
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
	 * No args constructor for chatmessage.
	 */
	public ChatMessage() {
		super();
	}
	
	
	
}
