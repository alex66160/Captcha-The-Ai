package com.captchatheai.backend.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StompController {

	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/session-id")
	public void getSessionId(StompHeaderAccessor accessor, @Payload ThrowawayUUIDRequest throwawayUUIDRequest) {

		messagingTemplate.convertAndSend("/queue/session-id/" + throwawayUUIDRequest.throwawayUUID(),
				new SessionIdResponse(accessor.getSessionId()));

	}

}
