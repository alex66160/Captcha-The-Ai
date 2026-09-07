package com.captchatheai.backend.websocket;

/**
 * The SessionIdResponse record contains the player's sessionId for subscribing
 * and sending messages.
 * 
 * @author Alex Liu
 * @param sessionId the player's sessionId
 */
public record SessionIdResponse(String sessionId) {

}
