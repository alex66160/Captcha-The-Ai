import { useContext } from "react";
import { lobbyContext } from "../LobbyContext";

function AnswerAnnouncement() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error(
            "Lobby State was null in answer announcement component",
        );
    }
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
