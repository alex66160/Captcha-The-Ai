package com.captchatheai.backend.lobby;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * In memory repository for the lobbies. Game uses in memory map and not MySQL database
 * since the lobbies and players are short lived and not meant to be persisted.
 * 
 * @author Alex Liu
 */
@Repository
public class LobbyRepository {

	ConcurrentHashMap<String, Lobby> lobbies = new ConcurrentHashMap<String, Lobby>();
	
	
	
}
