package com.captchatheai.backend.player;


import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Player {

	private UUID id;
	private String name;
	private PlayerAvatar avatar;
	private PlayerState state;


	private String sessionId;
	private boolean toBeKicked;
}
