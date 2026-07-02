package com.captchatheai.backend.lobby;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the lobby.
 * @author Alex Liu
 */
@Repository
public interface LobbyRepository extends JpaRepository<Lobby, String> {

}
