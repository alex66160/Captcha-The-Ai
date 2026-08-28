import { type PlayerAvatar } from "../LobbyTypes";
import { useState, useEffect } from "react";
import { getVotes } from "../LobbyService";
import { useParams } from "react-router-dom";

type VoterResponse = { playerName: string; playerAvatar: PlayerAvatar };
type VoteTargetResponse = {
    playerName: string;
    playerAvatar: PlayerAvatar;
    voters: VoterResponse[];
};
type VotesResponse = { voteTargets: VoteTargetResponse[] };

function Reveal() {
    const [votesResponse, setVotesResponse] = useState<VotesResponse | null>(
        null,
    );
    const lobbyId = useParams().lobbyId;
    if (lobbyId === undefined) {
        throw new Error("LobbyId was undefined in reveal component.");
    }
    useEffect(() => {
        getVotes(lobbyId).then((response) => setVotesResponse(response.data));
    }, [lobbyId]);

    return votesResponse === null
        ? null
        : votesResponse.voteTargets.map((voteTargetResponse, index) => (
              <div key={index}>
                  <p>
                      voteTarget: {voteTargetResponse.playerName}{" "}
                      {voteTargetResponse.playerAvatar}{" "}
                  </p>
                  {voteTargetResponse.voters.map((voterResponse) => (
                      <div key={voterResponse.playerName}>
                          <p>
                              voter: {voterResponse.playerName}{" "}
                              {voterResponse.playerAvatar}{" "}
                          </p>
                      </div>
                  ))}
              </div>
          ));
}

export default Reveal;
