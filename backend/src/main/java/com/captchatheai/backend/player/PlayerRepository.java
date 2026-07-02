package com.captchatheai.backend.player;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the Player.
 * 
 * @author Alex Liu
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long>{

	/** Repository method to find the list of players according to their lobby id */
	List<Player> findByLobbyLobbyId(String lobbyId);
}
