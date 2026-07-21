package com.captchatheai.backend.player;


import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Player {

	private UUID id;
	private String sessionId;
	
	private String name;
	private PlayerAvatar avatar;
	private PlayerState state;

	private Instant lastChatTime;



}
