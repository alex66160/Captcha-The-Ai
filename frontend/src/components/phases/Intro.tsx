import { useContext } from "react";
import { lobbyContext } from "../LobbyContext";

/**
 * The Intro component displays the players self identity through their name and player avatar.
 * @author Alex Liu
 */
function Intro() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error("Lobby state was null in Intro phase.");
    }

    const selfPlayer = lobbyState.players.filter((player) => player.isSelf)[0];

    return (
        <p>
            Your name is {selfPlayer.playerName} {selfPlayer.playerAvatar}
        </p>
    );
}

export default Intro;
