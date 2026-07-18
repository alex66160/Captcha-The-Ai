package com.captchatheai.backend.question;


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
	private String sessionId;

	
	
}
