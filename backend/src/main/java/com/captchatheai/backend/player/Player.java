package com.captchatheai.backend.player;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The Player model represents a player inside of a given lobby.
 * 
 * @author Alex Liu
 */
@Getter
@Setter
@AllArgsConstructor
public class Player {

	/** The player id is the players primary form of identification */
	private final UUID id = UUID.randomUUID();
	/**
	 * The sessionId is stored to verify player interactions across stomp websockets
	 */
	private String sessionId;

	private String name;
	private PlayerAvatar avatar;

	private PlayerStatus status;

	/** The lastChatTime is stored to prevent players from spamming chat messages */
	private Instant lastChatTime;

}
