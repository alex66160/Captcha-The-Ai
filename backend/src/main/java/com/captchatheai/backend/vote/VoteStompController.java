package com.captchatheai.backend.vote;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.captchatheai.backend.player.PlayerService;

import lombok.RequiredArgsConstructor;

/**
 * The VoteStompController class lets players send a vote to a given lobby.
 * 
 * @author Alex Liu
 */
@Controller
@RequiredArgsConstructor
public class VoteStompController {

	private final PlayerService playerService;

	private final VoteService voteService;

	/**
	 * The sendVote endpoint lets players send a vote to a given lobby.
	 * 
	 * @param lobbyId           the lobbyId to send the vote to
	 * @param accessor          the header accessor to get the sessionId of the
	 *                          voter
	 * @param submitVoteRequest the request that contains the vote target name
	 */
	@MessageMapping("/lobbies/{lobbyId}/votes")
	public void sendVote(@DestinationVariable int lobbyId, StompHeaderAccessor accessor,
			SubmitVoteRequest submitVoteRequest) {
		String sessionId = accessor.getSessionId();
		UUID voterId = playerService.getPlayerIdBySessionId(lobbyId, sessionId);
		UUID voteTargetId = playerService.getPlayerIdByName(lobbyId, submitVoteRequest.voteTargetName());

		voteService.sendVote(lobbyId, voterId, voteTargetId);
	}
}
