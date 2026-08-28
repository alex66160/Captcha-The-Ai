import { lobbyContext } from "./LobbyContext.ts";
import { useContext } from "react";
import { type PlayerState } from "./LobbyTypes.ts";

/**
 * The PlayerList component displays all the players in a lobby such as ALIVE, HIDDEN, and SPECTATOR players
 * but not disconnected players.
 * @author Alex Liu
 */
function PlayerList() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error("Lobby state is not supposed to be null");
    }
    const players: PlayerState[] = lobbyState.players;

    return (
        <div>
            {/* 
            We use index to keep track of the elements in the map in between renders rather than storing something 
            like the playerIds since it would expose who the Ai player is when the backend sends that info to the frontend.
            */}
            {players.map((player, index) => (
                <p key={index}>{player.playerName}</p>
            ))}
        </div>
    );
}

export default PlayerList;
