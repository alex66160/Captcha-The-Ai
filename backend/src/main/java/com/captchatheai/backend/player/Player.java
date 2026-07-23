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

	private final UUID id = UUID.randomUUID();
	
	private String name;
	private PlayerAvatar avatar;
	private PlayerState state;

	private Instant lastChatTime;



}
