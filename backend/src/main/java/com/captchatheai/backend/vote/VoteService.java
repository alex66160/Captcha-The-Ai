package com.captchatheai.backend.vote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.captchatheai.backend.lobby.Lobby;
import com.captchatheai.backend.lobby.LobbyPhase;
import com.captchatheai.backend.lobby.LobbyService;
import com.captchatheai.backend.player.Player;
import com.captchatheai.backend.player.PlayerService;
import com.captchatheai.backend.player.PlayerStatus;
import com.captchatheai.backend.vote.exception.AlreadyVotedException;
import com.captchatheai.backend.vote.exception.GetTiedPlayersDeniedException;
import com.captchatheai.backend.vote.exception.GetVotesDeniedException;
import com.captchatheai.backend.vote.exception.VotingDeniedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The VoteService class allows players to get votes, get tied players, and send
 * a vote. In addition, it can calculate votes which automatically sets the
 * voted out players and builds a tied player id list if there is a tie. Also
 * contains a method to clear votes, which includes the list of tied players.
 * 
 * @author Alex Liu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService {

	private final LobbyService lobbyService;

	private final PlayerService playerService;

	/**
	 * The getVotes method will return the votes in a lobby as the list of players,
	 * and each player with the list of players that voted for them.
	 * 
	 * @param lobbyId the lobbyId to get the votes from
	 * @return the votes in the lobby
	 * @throws GetVotesDeniedException if the lobby is not in the reveal phase
	 */
	public VotesResponse getVotes(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {

			if (lobby.getPhase() != LobbyPhase.REVEAL) {
				throw new GetVotesDeniedException(
						"Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount() + ", Lobby Phase: "
								+ lobby.getPhase() + ", Get votes denied: Lobby is not in REVEAL phase yet.");
			}

			Map<UUID, List<UUID>> votersByVoteTarget = lobby.getVotersByVoteTarget();

			// Construct a list of Vote targets.
			List<VoteTargetResponse> voteTargets = votersByVoteTarget.entrySet().stream().map((entry) -> {

				Player voteTargetPlayer = playerService.getPlayerById(lobbyId, entry.getKey());

				// Within each vote target, construct the list of voters that voted for them.
				List<VoterResponse> voters = entry.getValue().stream().map((voterId) -> {
					Player voter = playerService.getPlayerById(lobbyId, voterId);
					return new VoterResponse(voter.getName(), voter.getAvatar());
				}).toList();

				return new VoteTargetResponse(voteTargetPlayer.getName(), voteTargetPlayer.getAvatar(), voters);
			}).toList();

			log.info("Lobby Id: {}, Lobby Round: {}, Get votes was successfully ran.", lobbyId, lobby.getRoundCount());
			return new VotesResponse(voteTargets);
		}

	}

	/**
	 * The getTiedPlayers method returns the list of tied players.
	 * 
	 * @param lobbyId the lobbyId to get the tied players from
	 * @return the tied players from the lobby
	 * @throws GetTiedPlayersDeniedException if the lobby is not in the REVEAL_TIE
	 *                                       phase
	 */
	public TiedPlayersResponse getTiedPlayers(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			if (lobby.getPhase() != LobbyPhase.REVEAL_TIE) {
				throw new GetTiedPlayersDeniedException("Lobby Id: " + lobbyId + ", Lobby Round: "
						+ lobby.getRoundCount() + ", Lobby Phase: " + lobby.getPhase()
						+ ", Get tied players denied: Lobby is not in REVEAL_TIE phase yet.");
			}
			List<UUID> tiedPlayerIds = lobby.getTiedPlayerIds();
			List<TiedPlayerResponse> tiedPlayers = tiedPlayerIds.stream().map((tiedPlayerId) -> {

				Player tiedPlayer = playerService.getPlayerById(lobbyId, tiedPlayerId);
				return new TiedPlayerResponse(tiedPlayer.getName(), tiedPlayer.getAvatar(),
						tiedPlayerId.equals(lobby.getEliminatedPlayerId()));

			}).toList();
			log.info("Lobby Id: {}, Lobby Round: {}, Get tied players was successfully ran.", lobbyId,
					lobby.getRoundCount());
			return new TiedPlayersResponse(tiedPlayers);

		}

	}

	/**
	 * The sendVote method allows a player to send a vote to the lobby.
	 * 
	 * @param lobbyId      the lobbyId to send the vote to
	 * @param voterId      the players own id
	 * @param voteTargetId the player id that the player intends to vote for
	 * @throws VotingDeniedException if the lobby is not in the voting phase or if
	 *                               either the voter or vote target is not alive
	 * @throws AlreadyVotedException if the player tries to vote again after already
	 *                               voting
	 */
	public void sendVote(int lobbyId, UUID voterId, UUID voteTargetId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {

			if (lobby.getPhase() != LobbyPhase.VOTING) {
				throw new VotingDeniedException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ ", Lobby Phase: " + lobby.getPhase() + ", Voter Id: " + voterId + ", Vote Target Id: "
						+ voteTargetId + ", Send vote denied: Lobby is not in VOTING phase.");
			}

			Player voterPlayer = playerService.getPlayerById(lobbyId, voterId);

			Player voteTargetPlayer = playerService.getPlayerById(lobbyId, voteTargetId);

			if (voterPlayer.getStatus() != PlayerStatus.ALIVE) {
				throw new VotingDeniedException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ ", Voter Id: " + voterId + ", Vote Target Id: " + voteTargetId
						+ ", Send vote denied: Player sending the vote is not alive.");
			}

			if (voteTargetPlayer.getStatus() != PlayerStatus.ALIVE) {
				throw new VotingDeniedException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ ", Voter Id: " + voterId + ", Vote Target Id: " + voteTargetId
						+ ", Send vote denied: Player to receive the vote is not alive.");
			}

			Map<UUID, UUID> voteTargetByVoter = lobby.getVoteTargetByVoter();
			Map<UUID, List<UUID>> votersByVoteTarget = lobby.getVotersByVoteTarget();

			if (voteTargetByVoter.containsKey(voterId)) {
				throw new AlreadyVotedException("Lobby Id: " + lobbyId + ", Lobby Round: " + lobby.getRoundCount()
						+ ", Voter Id: " + voterId + ", Send vote denied: Player already voted.");
			}

			voteTargetByVoter.put(voterId, voteTargetId);
			// Get the arraylist if it already exists then add the voter, or make a new one
			// then add the voter.
			votersByVoteTarget.computeIfAbsent(voteTargetId, (key) -> new ArrayList<>()).add(voterId);
			log.info(
					"Lobby Id: {}, Lobby Round: {}, Voter Id: {}, Vote Target Id: {}, Vote was successfully submitted.",
					lobbyId, lobby.getRoundCount(), voterId, voteTargetId);
			// If all players have finished voting, we can skip ahead to the next phase.
			if (voteTargetByVoter.size() == lobby.getAlivePlayerCount()) {
				log.info("Lobby Id: {}, Lobby Round: {}, Voting has finished early.", lobbyId, lobby.getRoundCount());
				lobbyService.transitionToPhase(lobbyId, LobbyPhase.REVEAL_ANNOUNCEMENT);
			}

		}

	}

	/**
	 * The calculateVotes method calculates the votes in a lobby and sets the player
	 * to be voted out, in addition to constructing the tiedPlayerIds list so that
	 * the tied players can be stored.
	 * 
	 * @param lobbyId the lobbyId to calculate the votes on
	 * 
	 */
	public void calculateVotes(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {

			Map<UUID, List<UUID>> votersByVoteTarget = lobby.getVotersByVoteTarget();

			List<UUID> tiedPlayerIds = lobby.getTiedPlayerIds();

			// Firstly, find out what the maximum vote count is from the list of players.
			int maxVoteCount = 0;
			for (List<UUID> voter : votersByVoteTarget.values()) {
				maxVoteCount = Math.max(maxVoteCount, voter.size());
			}

			// Then, add the vote target that match the maximum vote count to the list of
			// tied players.
			for (Map.Entry<UUID, List<UUID>> entry : votersByVoteTarget.entrySet()) {
				if (entry.getValue().size() == maxVoteCount) {
					tiedPlayerIds.add(entry.getKey());
				}
			}

			// Basically, if there is more than one entry, we have a tie.
			// If there is a tie, choose a random player to be voted out.
			if (tiedPlayerIds.size() == 1) {

				lobby.setEliminatedPlayerId(tiedPlayerIds.getFirst());
				log.info("Lobby Id: {}, Lobby Round: {}, Voted Out Player Id: {}, There was no tie for this round.",
						lobbyId, lobby.getRoundCount(), lobby.getEliminatedPlayerId());
			} else {

				lobby.setEliminatedPlayerId(
						tiedPlayerIds.get(ThreadLocalRandom.current().nextInt(tiedPlayerIds.size())));

				log.info(
						"Lobby Id: {}, Lobby Round: {}, Voted Out Player Id: {}, Tied Player Ids: {}, There was a tie for this round.",
						lobbyId, lobby.getRoundCount(), lobby.getEliminatedPlayerId(), tiedPlayerIds.toString());
			}

		}
	}

	/**
	 * The clearVotes method clears all the votes in a lobby and the tied players.
	 * 
	 * @param lobbyId the lobbyId to clear the votes on
	 */
	public void clearVotes(int lobbyId) {
		Lobby lobby = lobbyService.getLobbyById(lobbyId);
		synchronized (lobby) {
			lobby.getVoteTargetByVoter().clear();
			lobby.getVotersByVoteTarget().clear();
			lobby.getTiedPlayerIds().clear();
			log.info("Lobby Id: {}, Lobby Round: {}, Votes and tied players were successfully cleared.");

		}
	}

}
