package com.captchatheai.backend.lobby;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import java.util.concurrent.CopyOnWriteArrayList;


import com.captchatheai.backend.chat.ChatMessage;
import com.captchatheai.backend.player.Player;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


/**
 * Lobby object that contains players, questions, answers,
 */
@Getter
@Setter
@RequiredArgsConstructor
public class Lobby {
	
	private final int id;
	
	private final String password;
	
	
	private UUID aiPlayerId;
	// we need to keep an ordered list of the playerids to show on the playerlist
	
	private final List<UUID> playerIds = new ArrayList<>();
	private final Map<UUID, Player> playersById = new HashMap<>();
	private final Map<String, UUID> playerIdsBySessionId = new HashMap<>();
	private final Map<String, UUID> playerIdsByName = new HashMap<>();
	
	
	
	
	private UUID eliminatedPlayerId;
	
	
	
	
	private final List<UUID> tiedPlayerIds = new ArrayList<>();
	
	private UUID questionWriterId;
	private String question;
	private final Map<UUID, String> answersById = new HashMap<>();
	
	
	private final Map<UUID, UUID> voteTargetByVoter = new HashMap<>();
	private final Map<UUID, List<UUID>> VotersByVoteTarget = new HashMap<>();

	
	
	private final Deque<ChatMessage> chatHistory = new ArrayDeque<>();
	
	private final List<String> gameHistory = new ArrayList<>();
	
	private LobbyPhase phase = LobbyPhase.INTERMISSION;
	private Instant phaseEndTime;
	
	private Instant gameStartTime;
	private int roundCount = 0;
}
