import { useState, useEffect } from "react";
import { type PlayerAvatar } from "../LobbyTypes";
import { useParams } from "react-router-dom";
import { getTiedPlayers } from "../LobbyService";

type TiedPlayerResponse = {
    playerName: string;
    playerAvatar: PlayerAvatar;
    isVotedOut: boolean;
};
type TiedPlayersResponse = { tiedPlayers: TiedPlayerResponse[] };

function RevealTie() {
    const lobbyId = useParams().lobbyId;
    if (lobbyId === undefined) {
        throw new Error("LobbyId is undefined in reveal tie component");
    }

    const [tiedPlayersResponse, setTiedPlayersResponse] =
        useState<TiedPlayersResponse | null>(null);

    useEffect(() => {
        getTiedPlayers(lobbyId).then((response) =>
            setTiedPlayersResponse(response.data),
        );
    });

    return tiedPlayersResponse === null ? null : (
        <div>
            <p>There is a tie between...</p>
            {tiedPlayersResponse.tiedPlayers.map(
                (tiedPlayerResponse, index) => (
                    <p key={index}>
                        {tiedPlayerResponse.playerName}{" "}
                        {tiedPlayerResponse.playerAvatar}
                    </p>
                ),
            )}
        </div>
    );
}

export default RevealTie;
