package com.captchatheai.backend.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
/**
 * The StompController class allows a player to get their sessionId.
 * 
 * @author Alex Liu
 */
public class StompController {

	private final SimpMessagingTemplate messagingTemplate;

	/**
	 * The getSessionId method allows a player to get their sessionId. We need this
	 * method because the sessionId is not sent back on the connect frame, so the
	 * player must make a temporary UUID so we have a destination to send the
	 * sessionId back when they call this endpoint.
	 * 
	 * @param accessor             the accessor for the player's sessionId
	 * @param throwawayUUIDRequest the temporary UUID for the player
	 */
	@MessageMapping("/session-id")
	public void getSessionId(StompHeaderAccessor accessor, @Payload ThrowawayUUIDRequest throwawayUUIDRequest) {

		messagingTemplate.convertAndSend("/queue/session-id/" + throwawayUUIDRequest.throwawayUUID(),
				new SessionIdResponse(accessor.getSessionId()));

	}

}
