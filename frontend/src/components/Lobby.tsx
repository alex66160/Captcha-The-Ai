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

function Lobby() {
    const lobbyState = useContext(lobbyContext);
    const lobbyId = useParams().lobbyId;
    const navigate = useNavigate();
    useEffect(() => {
        subscribe(
            `/api/lobbies/${lobbyId}/disconnect/${getSessionId()}`,
            () => {
                disconnect();
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
