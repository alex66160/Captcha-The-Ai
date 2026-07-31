package com.captchatheai.backend.player;

public enum PlayerAvatar {
	DOG("Dog"),
	WOLF("Wolf"),
	FOX("Fox"),
	RACCOON("Raccoon"),
	CAT("Cat"),
	MOUSE("Mouse"),
	RABBIT("Rabbit"),
	BEAR("Bear"),
	PANDA("Panda"),
	BIRD("Bird"),
	PENGUIN("Penguin"),
	EAGLE("Eagle"),
	FROG("Frog"),
	TURTLE("Turtle"),
	SNAKE("Snake"),
	WHALE("Whale"),
	DOLPHIN("Dolphin"),
	SEAL("Seal"),
	SHARK("Shark"),
	OCTOPUS("Octopus"),
	CRAB("Crab"),
	SPECTATOR("Spectator"),
	HIDDEN("Hidden");
	
	private final String name;
	
	private PlayerAvatar(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
