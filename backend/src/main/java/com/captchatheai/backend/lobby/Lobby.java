package com.captchatheai.backend.lobby;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.captchatheai.backend.answer.Answer;
import com.captchatheai.backend.chatmessage.ChatMessage;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.question.Question;

import lombok.Getter;
import lombok.Setter;


/**
 * Lobby object that contains players, questions, answers,
 */
@Getter
@Setter

public class Lobby {
	
	private String id;
	
	private String password;
	
	private String questionWriterSessionId;
	private String adminSessionId;
	private List<Player> players = new ArrayList<>();
	private Map<String, Player> sessionIdToPlayer = new HashMap<>();
	private Map<String, Player> nameToPlayer = new HashMap<>();
	private String kickedPlayerSessionId;
	
	private List<String> tiedPlayerNames = new ArrayList<>();
	
	private Queue<ChatMessage> chatMessages = new ArrayDeque<>();
	private List<Answer> answers = new ArrayList<>();
	private Question question;
	
	private Map<String, String> voterToVoteTarget;
	
	private LobbyPhase lobbyPhase;
	private Instant phaseEndTime;
	
	private int currentRound;
}
