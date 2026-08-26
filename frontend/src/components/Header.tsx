import { useContext, useState, useEffect } from "react";
import { lobbyContext } from "./LobbyContext";

function Header() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error("Lobby State was null in Header.");
    }

    const [secondsLeftForPhase, setSecondsLeftForPhase] = useState(0);

    useEffect(() => {
        const phaseEndTimeInterval = setInterval(() => {
            setSecondsLeftForPhase(
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
    let headerMessage = "";

    switch (lobbyState.lobbyPhase) {
        case "STARTING":
            headerMessage = `Game begins in ${secondsLeftForPhase} seconds`;
            break;
        case "QUESTION":
            headerMessage = `Question writing ends in ${secondsLeftForPhase} seconds`;
            break;
        case "ANSWER":
            headerMessage = `Answer writing ends in ${secondsLeftForPhase} seconds`;
            break;
        case "DISCUSS":
            headerMessage = `Discussion ends in ${secondsLeftForPhase} seconds`;
            break;
        case "VOTING":
            headerMessage = `Voting ends in ${secondsLeftForPhase} seconds`;
            break;
    }

    return (
        <div>
            <p> Round count: {lobbyState.roundCount}</p>
            <p>{headerMessage}</p>
        </div>
    );
}

export default Header;
