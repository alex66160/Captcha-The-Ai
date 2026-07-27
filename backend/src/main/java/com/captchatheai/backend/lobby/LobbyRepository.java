package com.captchatheai.backend.lobby;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

	private final Map<Integer, Lobby> lobbyById = new ConcurrentHashMap<>();
	
	public Optional<Lobby> findById(int id) {
		return Optional.ofNullable(lobbyById.get(id));
	}
	
	public Collection<Lobby> findAll() {
		return lobbyById.values();
	}
	
	public void save(Lobby lobby) {
		lobbyById.put(lobby.getId(), lobby);
	}
	
	public void deleteById(int id) {
		lobbyById.remove(id);
	}
	
	
	public long count() {
		return lobbyById.size();
	}
}
