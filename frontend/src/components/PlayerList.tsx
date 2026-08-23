import { useLobbyContext } from "./LobbyContext.ts";
import { type PlayerState } from "./LobbyTypes.ts";

function PlayerList() {
    const lobbyState = useLobbyContext();
    if (lobbyState === null) {
        throw new Error("Lobby state is not supposed to be null");
    }
    const players: PlayerState[] = lobbyState.players;

    return (
        <div>
            {players.map((player) => (
                <div>
                    <p>{player.playerName}</p>
                </div>
            ))}
        </div>
    );
}

export default PlayerList;
