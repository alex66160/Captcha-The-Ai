package com.captchatheai.backend.lobby;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import java.util.concurrent.CopyOnWriteArrayList;

import com.captchatheai.backend.answer.Answer;
import com.captchatheai.backend.chatmessage.ChatMessage;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.question.Question;

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
	private UUID questionWriterId;
	private UUID adminId;
	private List<Player> players = new CopyOnWriteArrayList<>();
	private Map<String, Player> sessionIdToPlayer = new ConcurrentHashMap<>();
	private Map<UUID, Player> idToPlayer = new ConcurrentHashMap<>();
	private UUID eliminatedPlayerId;
	
	private List<UUID> tiedPlayerId = new CopyOnWriteArrayList<>();
	
	private Deque<ChatMessage> chatHistory = new ConcurrentLinkedDeque<>();
	private List<Answer> answers = new CopyOnWriteArrayList<>();
	private Question question;
	
	private Map<UUID, UUID> voterToVoteTarget;
	
	private LobbyPhase lobbyPhase;
	private Instant phaseEndTime;
	
	private int currentRound;
}
