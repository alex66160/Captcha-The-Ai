package com.captchatheai.backend.chat;

/**
 * The SentChatMessageEvent record represents a chat message that was sent by a
 * player.
 * 
 * @author Alex Liu
 * @param playerName the player that sent the message
 * @param message    the message that was sent
 */
public record SentChatMessageEvent(String playerName, String message) {

}
