import { useContext } from "react";
import { lobbyContext } from "../LobbyContext";

/**
 * The DiscussAnnouncement component displays an message that discussion will start soon.
 * @author Alex Liu
 */
function DiscussAnnouncement() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error(
            "Lobby state is null in the discuss announcement component.",
        );
    }

    // Get the players self identity so we know what message to display.
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
