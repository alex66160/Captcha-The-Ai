import { lobbyContext } from "../LobbyContext";
import { useContext, useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { getEliminatedPlayer } from "../LobbyService";
import { type EliminatedPlayerResponse } from "../LobbyTypes";

function QuestionDisconnect() {
    const lobbyState = useContext(lobbyContext);
    const lobbyId = useParams().lobbyId;
    const [oldQuestionWriterName, setOldQuestionWriterName] = useState("");
    if (lobbyState === null) {
        throw new Error("Lobby state was null in question disconnect.");
    }
    if (lobbyId === undefined) {
        throw new Error("Lobby Id was null");
    }

    const questionWriter = lobbyState.players.filter(
        (player) => player.isQuestionWriter,
    )[0];

    useEffect(() => {
        getEliminatedPlayer(lobbyId).then((response) => {
            const eliminatedPlayerResponse: EliminatedPlayerResponse =
                response.data;
            setOldQuestionWriterName(eliminatedPlayerResponse.playerName);
        });
    }, [lobbyId]);

    return (
        <p>
            {oldQuestionWriterName} disconnected,
            {questionWriter.playerName} will be writing the next question1
        </p>
    );
}

export default QuestionDisconnect;
