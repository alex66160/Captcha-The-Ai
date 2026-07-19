package com.captchatheai.backend.answer;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Answer {

	private String answer;
	private UUID playerId;

}
