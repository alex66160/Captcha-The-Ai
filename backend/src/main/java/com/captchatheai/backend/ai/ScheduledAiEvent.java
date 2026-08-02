package com.captchatheai.backend.ai;

import java.time.Instant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * The ScheduledAiEvent class represents the event that needs to be ran for the
 * Ai player in the given lobby with the time to execute.
 * 
 * @author Alex Liu
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ScheduledAiEvent {
	private final AiEvent aiEvent;
	private final Instant timeToExecute;
}
