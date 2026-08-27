import { useContext } from "react";
import { lobbyContext } from "../LobbyContext";

function DiscussAnnouncement() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error(
            "Lobby state is null in the discuss announcement component.",
        );
    }

    const selfPlayer = lobbyState.players.filter((player) => player.isSelf)[0];
    if (selfPlayer.playerStatus === "ALIVE") {
        return <p>Get ready to discuss and vote for the Ai player!</p>;
    }

    if (selfPlayer.playerStatus === "SPECTATOR") {
        return (
            <p>Other players will now discuss and vote for the Ai Player!</p>
        );
    }
}

export default DiscussAnnouncement;
