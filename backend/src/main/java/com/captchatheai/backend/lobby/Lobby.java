package com.captchatheai.backend.lobby;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.captchatheai.backend.ai.ScheduledAiEvent;
import com.captchatheai.backend.chat.ChatMessage;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * The Lobby model contains the entire state for a game, such as the id, mapping
 * of players for quick lookups, questions, answers, votes, chat history, and
 * game history.
 * 
 * @author Alex Liu
 */
@Getter
@Setter
@RequiredArgsConstructor
public class Lobby {

	private final int id = ThreadLocalRandom.current().nextInt(100000, 1000000);

	/** If the password of a lobby is null, it is public and private otherwise */
	private final String password;

	private UUID aiPlayerId;

	/** The playerIds list is meant to store the players in order */
	private final List<UUID> playerIds = new ArrayList<>();
	private final Map<UUID, Player> playersById = new HashMap<>();
	/**
	 * The playerIdsBySessionId map is primarily used for quick lookups when players
	 * disconnect
	 */
	private final Map<String, UUID> playerIdsBySessionId = new HashMap<>();
	/**
	 * The playerIdsByName map is primarily used when players send votes, as they
	 * can only send the player names when voting
	 */
	private final Map<String, UUID> playerIdsByName = new HashMap<>();

	/**
	 * The eliminatedPlayerId is used to store the player in situations where the
	 * frontend needs to display the eliminated players info. These situations
	 * include the question writer leaving during the question phase, question
	 * writer forgetting to write a question, getting voted out, and leaving during
	 * the voting phase when alive.
	 */
	private UUID eliminatedPlayerId;

	private final List<UUID> tiedPlayerIds = new ArrayList<>();

	private UUID questionWriterId;
	private String question;
	private final Map<UUID, String> answersById = new HashMap<>();

	/**
	 * The voteTargetByVoter map is used for quick lookups to check if a player has
	 * already voted
	 */
	private final Map<UUID, UUID> voteTargetByVoter = new HashMap<>();
	/**
	 * The votersByVoteTarget map is used to easily send back players and players
	 * that voted for that player when needed
	 */
	private final Map<UUID, List<UUID>> votersByVoteTarget = new HashMap<>();

	/** The chatHistory is used to provide chat context to the ai player. */
	private final Deque<ChatMessage> chatHistory = new ArrayDeque<>();

	/**
	 * The gameHistory list is used to provide major events that happened during a
	 * game to give the ai extra context
	 */
	private final List<String> gameHistory = new ArrayList<>();

	/**
	 * The scheduledAiEvent is a planned event for the ai such as generating a
	 * question, answer, or vote.
	 */
	private ScheduledAiEvent scheduledAiEvent;

	private LobbyPhase phase;
	private Instant phaseEndTime;

	/** The gameStartTime is stored so that chat messages can be time stamped. */
	private Instant gameStartTime;

	private int roundCount = 1;

	/**
	 * The getPlayerCount method returns the number of players that are not
	 * DISCONNECTED.
	 * 
	 * @return the number of non disconnected players
	 */
	public int getPlayerCount() {
		return (int) playersById.values().stream().filter((player) -> player.getStatus() != PlayerStatus.DISCONNECTED)
				.count();
	}

	/**
	 * The getPlayerCount method returns the number of players that are ALIVE.
	 * 
	 * @return the number of alive players
	 */
	public int getAlivePlayerCount() {
		return (int) playersById.values().stream().filter((player) -> player.getStatus() == PlayerStatus.ALIVE).count();
	}
}
