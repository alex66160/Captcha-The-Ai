import { useContext, useState, useEffect } from "react";
import { lobbyContext } from "../LobbyContext";
import { type PlayerAvatar } from "../LobbyTypes";
import { useParams } from "react-router-dom";

import { getAiPlayer } from "../LobbyService";

type AiPlayerResponse = { playerName: string; playerAvatar: PlayerAvatar };

function GameResult() {
    const lobbyState = useContext(lobbyContext);
    const lobbyId = useParams().lobbyId;
    const [aiPlayerResponse, setAiPlayerResponse] =
        useState<AiPlayerResponse | null>(null);
    if (lobbyState === null) {
        throw new Error("Lobby state was null in game result component");
    }
    if (lobbyId === undefined) {
        throw new Error("Lobby Id was null");
    }

    useEffect(() => {
        getAiPlayer(lobbyId).then((response) => {
            setAiPlayerResponse(response.data);
        });
    }, [lobbyId]);

    let gameResultMessage = "";
    switch (lobbyState.lobbyPhase) {
        case "AI_PLAYER_WON":
            gameResultMessage =
                "The Ai tricked everyone! Better luck next time...";
            break;
        case "AI_PLAYER_FAILED_TO_RESPOND":
            gameResultMessage = "The Ai failed to respond, oops.";
            break;
        case "HUMAN_PLAYERS_WON":
            gameResultMessage = "The Ai was captured! You won!";
            break;
        case "NOT_ENOUGH_PLAYERS":
            gameResultMessage = "Too many players left, the Ai wins!";
            break;
    }

    return aiPlayerResponse === null ? null : (
        <div>
            <p>{gameResultMessage}</p>
            <p>
                The Ai was: {aiPlayerResponse.playerName}{" "}
                {aiPlayerResponse.playerAvatar}
            </p>
        </div>
    );
}

export default GameResult;
