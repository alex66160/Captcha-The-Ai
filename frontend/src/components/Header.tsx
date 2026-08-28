import { useContext, useState, useEffect } from "react";
import { lobbyContext } from "./LobbyContext";
import { useParams, useNavigate } from "react-router-dom";
import { publish, disconnect } from "./StompActions";

/**
 * The Header component is responsible for showing the header message which contains the seconds left for certain phases,
 * the round count, as well as allowing players to leave a lobby.
 * @author Alex Liu
 */
function Header() {
    const lobbyState = useContext(lobbyContext);
    const lobbyId = useParams().lobbyId;
    const navigate = useNavigate();
    if (lobbyState === null) {
        throw new Error("Lobby State was null in Header.");
    }
    if (lobbyId === undefined) {
        throw new Error("Lobby id was undefined in header.");
    }

    const [secondsLeftForPhase, setSecondsLeftForPhase] = useState(0);

    useEffect(() => {
        const phaseEndTimeInterval = setInterval(() => {
            setSecondsLeftForPhase(
                // We use math.max to avoid negative times (sometimes the timer may go over by a second),
                // and we use math.ceil to reduce the chances of the timer staying at zero for longer than a second.

                // The reason we dont do something like subtracting 1 to the secondsLeftForPhase after every second is so that
                // our timer doesnt drift due to inaccuracies, so we calculate it fresh every second from the phase end time to the current time
                // to make sure its as accurate as possible.
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
        case "AI_PLAYER_WON":
            headerMessage = `Next game begins in ${secondsLeftForPhase} seconds`;
            break;
        case "AI_PLAYER_FAILED_TO_RESPOND":
            headerMessage = `Game restarts in ${secondsLeftForPhase} seconds`;
            break;
        case "HUMAN_PLAYERS_WON":
            headerMessage = `Game restarts in ${secondsLeftForPhase} seconds`;
            break;
        case "NOT_ENOUGH_PLAYERS":
            headerMessage = `Game restarts in ${secondsLeftForPhase} seconds`;
            break;
    }

    return (
        <div>
            <button
                onClick={() => {
                    publish<null>(`/api/lobbies/${lobbyId}/leave`, null);
                    disconnect();
                    navigate("/");
                }}
            >
                leave lobby
            </button>

            <p> Round count: {lobbyState.roundCount}</p>
            <p>{headerMessage}</p>
        </div>
    );
}

export default Header;
