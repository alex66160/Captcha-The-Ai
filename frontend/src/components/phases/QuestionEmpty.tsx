import { lobbyContext } from "../LobbyContext";
import { useContext, useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { getEliminatedPlayer } from "../LobbyService";
import { type EliminatedPlayerResponse } from "../LobbyTypes";
/**
 * The QuestionEmpty component displays the current question writer who forgot to write a question and who the next question writer will be.
 * @author Alex Liu
 */
function QuestionEmpty() {
    const lobbyState = useContext(lobbyContext);
    const lobbyId = useParams().lobbyId;
    // The eliminated player is the question writer that forgot to write a question.
    const [eliminatedPlayerResponse, setEliminatedPlayerResponse] =
        useState<EliminatedPlayerResponse | null>(null);
    if (lobbyState === null) {
        throw new Error("Lobby state was null in question empty.");
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
            {eliminatedPlayerResponse.playerName} forgot to write a question,
            {questionWriter.playerName} will be writing the next question1
        </p>
    );
}

export default QuestionEmpty;
