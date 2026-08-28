import { useContext } from "react";
import { lobbyContext } from "../LobbyContext";

/**
 * The AnswerAnnouncement component displays an announcement message that answering will begin soon.
 * @author Alex Liu
 */
function AnswerAnnouncement() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error(
            "Lobby State was null in answer announcement component",
        );
    }
    // Get the players self identity so we know what message to display.
    const selfPlayer = lobbyState.players.filter((player) => player.isSelf)[0];
    if (selfPlayer.isQuestionWriter && selfPlayer.playerStatus === "ALIVE") {
        return <p>Players will now begin answering your question!</p>;
    }

    if (!selfPlayer.isQuestionWriter && selfPlayer.playerStatus === "ALIVE") {
        return <p>Get ready to answer the question!</p>;
    }

    if (selfPlayer.playerStatus === "SPECTATOR") {
        return <p>Players will now begin answering the question!</p>;
    }
}

export default AnswerAnnouncement;
