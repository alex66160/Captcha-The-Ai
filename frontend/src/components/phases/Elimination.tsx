import { lobbyContext } from "../LobbyContext";
import { useContext, useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { getEliminatedPlayer } from "../LobbyService";
import { type EliminatedPlayerResponse } from "../LobbyTypes";

/**
 * The Elimination component displays the player that was voted out, and whether or not that player was
 * the ai.
 * @author Alex Liu
 */
function Elimination() {
    const lobbyState = useContext(lobbyContext);
    const lobbyId = useParams().lobbyId;
    const [eliminatedPlayerResponse, setEliminatedPlayerResponse] =
        useState<EliminatedPlayerResponse | null>(null);
    if (lobbyState === null) {
        throw new Error("Lobby state was null in question disconnect.");
    }
    if (lobbyId === undefined) {
        throw new Error("Lobby Id was null");
    }

    useEffect(() => {
        getEliminatedPlayer(lobbyId).then((response) => {
            setEliminatedPlayerResponse(response.data);
        });
    }, [lobbyId]);

    return eliminatedPlayerResponse === null ? null : (
        <p>
            {eliminatedPlayerResponse.playerName} was voted out.
            {eliminatedPlayerResponse.playerAvatar}
            {eliminatedPlayerResponse.playerName} was{" "}
            {eliminatedPlayerResponse.isAi ? "" : "not"} the ai.
        </p>
    );
}

export default Elimination;
