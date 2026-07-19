package com.captchatheai.backend.question;


import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * The Question class represents a player's written question
 * when they are chosen as the question writer.
 * 
 * @author Alex Liu
 */


@Getter
@Setter
@AllArgsConstructor
public class Question {
	
	private String question;
	private UUID playerId;

	
	
}
