package com.captchatheai.backend.ai;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ScheduledAiEvent {
	private final AiEvent aiEvent;
	private final Instant timeToExecute;
}
