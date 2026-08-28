import { lobbyContext } from "../LobbyContext";
import { useContext, useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { getEliminatedPlayer } from "../LobbyService";
import { type EliminatedPlayerResponse } from "../LobbyTypes";

/**
 * The QuestionDisconnect component displays the current question writer who disconnected and who the next question writer will be.
 * @author Alex Liu
 */
function QuestionDisconnect() {
    const lobbyState = useContext(lobbyContext);
    const lobbyId = useParams().lobbyId;
    // The eliminated player is the question writer that disconnected.
    const [eliminatedPlayerResponse, setEliminatedPlayerResponse] =
        useState<EliminatedPlayerResponse | null>(null);

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
            setEliminatedPlayerResponse(response.data);
        });
    }, [lobbyId]);

    return eliminatedPlayerResponse === null ? null : (
        <p>
            {eliminatedPlayerResponse.playerName} disconnected,
            {questionWriter.playerName} will be writing the next question1
        </p>
    );
}

export default QuestionDisconnect;
