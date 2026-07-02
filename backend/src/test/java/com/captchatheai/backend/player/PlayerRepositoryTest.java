package com.captchatheai.backend.player;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.captchatheai.backend.lobby.GamePhase;
import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyRepository;



/**
 * Test method for the PlayerRepository.
 * 
 * @author Alex Liu
 */

@DataJpaTest
public class PlayerRepositoryTest {

	/** The playerRepository to test */
	@Autowired
	private PlayerRepository playerRepository;
	
	/** The lobbyRepository to test */
	@Autowired
	private LobbyRepository lobbyRepository;
	
	/** The saved player from the repository */
	private Player savedPlayer;
	
	@BeforeEach
	public void setup() {
		playerRepository.deleteAll();
		lobbyRepository.deleteAll();
		
		Lobby lobby = new Lobby("A1B2C3", "LobbyTestName", 1, 5, 2, GamePhase.INTERMISSION, 0, 8, null, 30, 30, 120);
		Lobby lobby2 = new Lobby("123456", "LobbyTestName", 1, 5, 2, GamePhase.INTERMISSION, 0, 8, null, 30, 30, 120);
		

		
		Lobby savedLobby = lobbyRepository.save(lobby);
		Lobby savedLobby2 = lobbyRepository.save(lobby2);
		
	    // When creating the new player, do not use "lobby"! "lobby" is untracked, we must store a tracked version
		// into the player we intent to persist. Storing lobby will cause the player to use a frozen snapshot of the lobby.
		Player player = new Player(null, "PlayerTestName", AvatarColor.AVATAR_BLUE, AvatarEyes.EYES_DEFAULT, AvatarMouth.MOUTH_DEFAULT
				, false, true, false, true, savedLobby);
		// we need to use a savedPlayer object because we wont know what the playerid is
		// when the player is saved to the database.
		savedPlayer = playerRepository.save(player);
		
		Player player2 = new Player(null, "Player2TestName", AvatarColor.AVATAR_BLUE, AvatarEyes.EYES_DEFAULT, AvatarMouth.MOUTH_DEFAULT
				, false, true, false, true, savedLobby2);
		
		playerRepository.save(player2);
		
	}
	
	/** Tests getting players by their lobby id */
	@Test
	public void testGetPlayersByLobbyId() {
		List<Player> players = playerRepository.findByLobbyLobbyId("A1B2C3");
		assertAll("Player list contents", 
				() -> assertEquals(1, players.size()), 
				() -> assertEquals(savedPlayer.getPlayerId(), players.get(0).getPlayerId()),
				() -> assertEquals(savedPlayer.getPlayerName(), players.get(0).getPlayerName()),
				() -> assertEquals(savedPlayer.getAvatarColor(), players.get(0).getAvatarColor()),
				() -> assertEquals(savedPlayer.getAvatarEyes(), players.get(0).getAvatarEyes()),
				() -> assertEquals(savedPlayer.getAvatarMouth(), players.get(0).getAvatarMouth()),
				() -> assertEquals(savedPlayer.isAi(), players.get(0).isAi()),
				() -> assertEquals(savedPlayer.isAlive(), players.get(0).isAlive()),
				() -> assertEquals(savedPlayer.isHasVoted(), players.get(0).isHasVoted()),
				() -> assertEquals(savedPlayer.isAdmin(), players.get(0).isAdmin()),
				() -> assertEquals(savedPlayer.getLobby().getLobbyId(), players.get(0).getLobby().getLobbyId()));
		
		
		List<Player> emptyPlayers = playerRepository.findByLobbyLobbyId("ABCDEF");
		assertTrue(emptyPlayers.isEmpty());
	}

}
