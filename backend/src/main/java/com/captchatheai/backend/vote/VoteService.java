package com.captchatheai.backend.vote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerState;
import com.captchatheai.backend.vote.exception.AlreadyVotedException;
import com.captchatheai.backend.vote.exception.GetTiedPlayersDeniedException;
import com.captchatheai.backend.vote.exception.GetVotesDeniedException;

import com.captchatheai.backend.vote.exception.VotingDeniedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {

	private final LobbyService lobbyService;
	
	private final PlayerService playerService;
	
	public VotesDto getVotes(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			if (lobby.getPhase() != LobbyPhase.REVEAL) {
				throw new GetVotesDeniedException();
			}
			
			Map<UUID, List<UUID>> votersByVoteTarget = lobby.getVotersByVoteTarget();
			
			List<VoteTargetDto> voteTargets = votersByVoteTarget.entrySet().stream().map((entry) -> {
				
				Player voteTargetPlayer = playerService.getPlayerById(lobbyId, entry.getKey());
				
				
				List<VoterDto> voters = entry.getValue().stream().map((voterId) -> {
					Player voter = playerService.getPlayerById(lobbyId, voterId);
					return new VoterDto(voter.getName(), voter.getAvatar());
					}).toList();
			
				
				
				return new VoteTargetDto(voteTargetPlayer.getName(), voteTargetPlayer.getAvatar(), voters);
				}).toList();
			
			
			return new VotesDto(voteTargets);
		}
		
	}
	
	public TiedPlayersDto getTiedPlayers(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			if (lobby.getPhase() != LobbyPhase.REVEAL_TIE) {
				throw new GetTiedPlayersDeniedException();
			}
			List<UUID> tiedPlayerIds = lobby.getTiedPlayerIds();
			List<TiedPlayerDto> tiedPlayers = tiedPlayerIds.stream().map((tiedPlayerId) -> {
				
				Player tiedPlayer = playerService.getPlayerById(lobbyId, tiedPlayerId);
				return new TiedPlayerDto(tiedPlayer.getName(), tiedPlayer.getAvatar(), tiedPlayerId.equals(lobby.getEliminatedPlayerId()));
				
				
				}).toList();
			
			return new TiedPlayersDto(tiedPlayers);
			
		}
		
	}
	
	
	public void sendVote(int lobbyId, UUID voterId, UUID voteTargetId) {
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
			Map<UUID, List<UUID>> votersByVoteTarget = lobby.getVotersByVoteTarget();
			if (voteTargetByVoter.containsKey(voterId)) {
				throw new AlreadyVotedException();
			}
			
			voteTargetByVoter.put(voterId, voteTargetId);
			votersByVoteTarget.computeIfAbsent(voteTargetId, (key) -> new ArrayList<>()).add(voterId);
//			if (votersByVoteTarget.get(voteTargetId) == null) {
//				votersByVoteTarget.put(voteTargetId, new HashSet<>());
//				
//			}
//			votersByVoteTarget.get(voteTargetId).add(voterId);
		}

	}
	
	public void calculateVotes (int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			
			Map<UUID, List<UUID>> votersByVoteTarget = lobby.getVotersByVoteTarget();
			
			List<UUID> tiedPlayerIds = lobby.getTiedPlayerIds();
			
			int maxVoteCount = 0;
			for (List<UUID> voter : votersByVoteTarget.values()) {
				maxVoteCount = Math.max(maxVoteCount, voter.size());
			}
			
			for (Map.Entry<UUID, List<UUID>> entry : votersByVoteTarget.entrySet()) {
				if (entry.getValue().size() == maxVoteCount) {
					tiedPlayerIds.add(entry.getKey());
				}
			}
			
			
			if (tiedPlayerIds.size() == 1) {
				lobby.setEliminatedPlayerId(tiedPlayerIds.getFirst());
			} else {
				
				lobby.setEliminatedPlayerId(tiedPlayerIds.get(ThreadLocalRandom.current().nextInt(tiedPlayerIds.size())));
			}
			
			
			
			
			
		}
	}
	
	public void clearVotes(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized(lobby) {
			lobby.getVoteTargetByVoter().clear();
			lobby.getVotersByVoteTarget().clear();
			lobby.getTiedPlayerIds().clear();
			
			
		}
	}

	
}
