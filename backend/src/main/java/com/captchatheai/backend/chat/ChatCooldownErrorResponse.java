package com.captchatheai.backend.chat;

/**
 * The ChatCooldownErrorResponse represents an error where the player tries to
 * send chat messages before their cooldown is over, and it includes the time
 * left before they can chat again.
 * 
 * @author Alex Liu
 * @param timeLeftOnCooldown the seconds before the player can chat again
 */
public record ChatCooldownErrorResponse(double timeLeftOnCooldown) {

}
