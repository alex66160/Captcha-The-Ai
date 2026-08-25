import { useContext, useState, useEffect } from "react";
import { lobbyContext } from "./LobbyContext";

function Header() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error("Lobby State was null in Header.");
    }

    const [phaseEndTime, setPhaseEndTime] = useState(0);
    const [headerMessage, setHeaderMessage] = useState("");
    useEffect(() => {
        const phaseEndTimeInterval = setInterval(() => {
            setPhaseEndTime(
                Math.max(
                    0,
                    Math.ceil(
                        (new Date(lobbyState.phaseEndTime).getTime() -
                            Date.now()) /
                            1000,
                    ),
                ),
            );
        }, 1000);

        return () => {
            clearInterval(phaseEndTimeInterval);
        };
    }, [lobbyState.phaseEndTime]);

    if (lobbyState.lobbyPhase === "STARTING") {
        setHeaderMessage(`Game begins in ${phaseEndTime} seconds`);
    } else if (lobbyState.lobbyPhase === "QUESTION") {
        setHeaderMessage(`Question writing ends in ${phaseEndTime} seconds`);
    } else if (lobbyState.lobbyPhase === "ANSWER") {
        setHeaderMessage(`Answer writing ends in ${phaseEndTime} seconds`);
    } else if (lobbyState.lobbyPhase === "DISCUSS") {
        setHeaderMessage(`Discussion ends in ${phaseEndTime} seconds`);
    } else if (lobbyState.lobbyPhase === "VOTING") {
        setHeaderMessage(`Voting ends in ${phaseEndTime} seconds`);
    } else {
        setHeaderMessage("");
    }

    return (
        <div>
            <p> Round count: {lobbyState.roundCount}</p>
            <p>{headerMessage}</p>
        </div>
    );
}

export default Header;
