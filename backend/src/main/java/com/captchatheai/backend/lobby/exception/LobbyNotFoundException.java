package com.captchatheai.backend.lobby.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;



public class LobbyNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LobbyNotFoundException(int id) {
		super("Lobby with id " + id + " was not found.");
	}
}
