import { useContext, useState } from "react";
import { lobbyContext } from "../LobbyContext";

function Intro() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error("Lobby state was null in Intro phase.");
    }
}

export default Intro;
