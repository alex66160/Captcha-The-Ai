package com.captchatheai.backend.chat;

/**
 * The ChatMessageSentEvent record represents a chat message that was sent by a
 * player.
 * 
 * @author Alex Liu
 * @param playerName the player that sent the message
 * @param message    the message that was sent
 */
public record ChatMessageSentEvent(String playerName, String message) {

}
