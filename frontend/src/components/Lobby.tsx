import PlayerList from "./PlayerList";
import GameScreen from "./GameScreen";
import ChatBox from "./ChatBox";
import Stats from "./Stats";
import LoadingScreen from "./LoadingScreen";
import Header from "./Header";
import { lobbyContext } from "./LobbyContext";
import { useContext, useEffect } from "react";
import { subscribe, getSessionId, disconnect } from "./StompActions";
import { useParams, useNavigate } from "react-router-dom";

/**
 * The Lobby component serves as a container for the header, playerlist, gamescreen, chatbox, and stats. It
 * is also responsible for listening to kick messages from the backend and navigates back to the home page
 * if it happens.
 * @author Alex Liu
 */
function Lobby() {
    const lobbyState = useContext(lobbyContext);
    const lobbyId = useParams().lobbyId;
    const navigate = useNavigate();
    useEffect(() => {
        subscribe(
            `/api/lobbies/${lobbyId}/disconnect/${getSessionId()}`,
            () => {
                disconnect();
                // Attach the kick message into the state so that the home page can use location.state
                // to determine that it got to the home page because the player was kicked.
                navigate("/", {
                    state: "You were kicked from the lobby for being AFK.",
                });
            },
        );
    }, [lobbyId, navigate]);

    return lobbyState === null ? (
        <LoadingScreen />
    ) : (
        <div>
            <Header />
            <PlayerList />
            <GameScreen />
            <ChatBox />
            <Stats />
        </div>
    );
}

export default Lobby;
