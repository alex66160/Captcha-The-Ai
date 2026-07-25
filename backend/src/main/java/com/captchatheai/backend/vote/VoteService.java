package com.captchatheai.backend.vote;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerState;
import com.captchatheai.backend.vote.exception.AlreadyVotedException;
import com.captchatheai.backend.vote.exception.GetVotesDeniedException;
import com.captchatheai.backend.vote.exception.NotVotingPhaseException;
import com.captchatheai.backend.vote.exception.VotingDeniedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {

	private final LobbyService lobbyService;
	
	private final PlayerService playerService;
	
	public VotesDto getVotes(String lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			if (lobby.getPhase() != LobbyPhase.REVEAL) {
				throw new GetVotesDeniedException();
			}
			
			Map<UUID, UUID> voteTargetByVoter = lobby.getVoteTargetByVoter();
			
			
			
			
		}
		
	}
	
	
	public void sendVote(String lobbyId, UUID voterId, UUID voteTargetId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			
			
			
			if (lobby.getPhase() != LobbyPhase.VOTING) {
				throw new VotingDeniedException();
			}
			
			Player voterPlayer = playerService.getPlayerById(lobbyId, voterId);
			
			Player voteTargetPlayer = playerService.getPlayerById(lobbyId, voteTargetId);
			
			
			if (voterPlayer.getState() != PlayerState.ALIVE || voteTargetPlayer.getState() != PlayerState.ALIVE) {
				throw new VotingDeniedException();
			}
			
			
			Map<UUID, UUID> voteTargetByVoter = lobby.getVoteTargetByVoter();
			Map<UUID, Set<UUID>> votersByVoteTarget = lobby.getVotersByVoteTarget();
			if (voteTargetByVoter.containsKey(voterId)) {
				throw new AlreadyVotedException();
			}
			
			voteTargetByVoter.put(voterId, voteTargetId);
			//votersByVoteTarget.computeIfAbsent(voteTargetId, (key) -> new HashSet<>()).add(voterId);
			if (votersByVoteTarget.get(voteTargetId) == null) {
				votersByVoteTarget.put(voteTargetId, new HashSet<>());
				
			}
			votersByVoteTarget.get(voteTargetId).add(voterId);
		}

	}
	
	

	
}
