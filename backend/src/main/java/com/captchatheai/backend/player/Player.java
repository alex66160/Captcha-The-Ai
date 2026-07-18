package com.captchatheai.backend.player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Player {

	private String name;
	private PlayerAvatar avatar;
	private PlayerState state;

	private boolean isAi;
	private String sessionId;
	private boolean toBeKicked;
}
