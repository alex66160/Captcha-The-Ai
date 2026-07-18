package com.captchatheai.backend.lobby;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
	
	private String questionWriterSessionId;
	private String adminSessionId;
	private List<Player> players = new CopyOnWriteArrayList<>();
	private Map<String, Player> sessionIdToPlayer = new ConcurrentHashMap<>();
	private Map<String, Player> nameToPlayer = new ConcurrentHashMap<>();
	private String playerToBeEliminatedSessionId;
	
	private List<String> tiedPlayerNames = new CopyOnWriteArrayList<>();
	
	private Queue<ChatMessage> chatMessages = new ConcurrentLinkedQueue<>();
	private List<Answer> answers = new CopyOnWriteArrayList<>();
	private Question question;
	
	private Map<String, String> voterToVoteTarget;
	
	private LobbyPhase lobbyPhase;
	private Instant phaseEndTime;
	
	private int currentRound;
}
