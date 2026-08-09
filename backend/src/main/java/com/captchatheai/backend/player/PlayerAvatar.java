package com.captchatheai.backend.player;

/**
 * The PlayerAvatar enum represents all the possible player avatars for a
 * player. Each avatar also includes its name as player names are assigned based
 * on which avatar they get.
 * 
 * @author Alex Liu
 */
public enum PlayerAvatar {
	MONKEY("Monkey"), DOG("Dog"), WOLF("Wolf"), FOX("Fox"), RACCOON("Raccoon"), CAT("Cat"), LION("Lion"),
	TIGER("Tiger"), COW("Cow"), PIG("Pig"), MOUSE("Mouse"), HAMSTER("Hamster"), RABBIT("Rabbit"), BEAR("Bear"),
	PANDA("Panda"), BIRD("Bird"), PENGUIN("Penguin"), EAGLE("Eagle"), DUCK("Duck"), FROG("Frog"), TURTLE("Turtle"),
	SNAKE("Snake"), WHALE("Whale"), DOLPHIN("Dolphin"), SEAL("Seal"), SHARK("Shark"), OCTOPUS("Octopus"), CRAB("Crab"),
	SPECTATOR("Spectator"), HIDDEN("Hidden");

	private final String name;

	private PlayerAvatar(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
