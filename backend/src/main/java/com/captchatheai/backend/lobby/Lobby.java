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
import lombok.Setter;


/**
 * Lobby object that contains players, questions, answers,
 */
@Getter
@Setter
@AllArgsConstructor
public class Lobby {
	
	private String id;
	
	private String password;
	
	
	private UUID aiPlayerId;
	// we need to keep an ordered list of the playerids to show on the playerlist
	
	private List<UUID> playerIds = new ArrayList<>();
	private Map<UUID, Player> playersById = new HashMap<>();
	private Map<String, Player> playersBySessionId = new HashMap<>();

	
	
	
	
	private UUID eliminatedPlayerId;
	private Set<UUID> toBeKickedPlayerIds = new HashSet<>();
	
	
	
	private Set<UUID> tiedPlayerIds = new HashSet<>();
	
	private UUID questionWriterId;
	private String question;
	private Map<UUID, String> answersById = new HashMap<>();
	
	
	private Map<UUID, UUID> voteTargetByVoter = new HashMap<>();
	

	
	
	private Deque<ChatMessage> chatHistory = new ArrayDeque<>();
	
	private List<String> gameHistory = new ArrayList<>();
	
	private LobbyPhase phase;
	private Instant phaseEndTime;
	
	private Instant gameStartTime;
	private int roundCount;
}
