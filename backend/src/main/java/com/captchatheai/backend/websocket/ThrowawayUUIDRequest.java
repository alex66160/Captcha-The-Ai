package com.captchatheai.backend.websocket;

import java.util.UUID;

/**
 * The ThrowawayUUIDRequest record represents a player's temporary UUID for
 * where we need the broadcast their sessionId to.
 * 
 * @author Alex Liu
 * @param throwawayUUID the players temporary UUID
 */
public record ThrowawayUUIDRequest(UUID throwawayUUID) {

}
