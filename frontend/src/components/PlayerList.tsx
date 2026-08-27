import { lobbyContext } from "./LobbyContext.ts";
import { useContext } from "react";
import { type PlayerState } from "./LobbyTypes.ts";

function PlayerList() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error("Lobby state is not supposed to be null");
    }
    const players: PlayerState[] = lobbyState.players;

    return (
        <div>
            {players.map((player) => (
                <p key={player.playerName}>{player.playerName}</p>
            ))}
        </div>
    );
}

export default PlayerList;
