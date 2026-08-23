import PlayerList from "./PlayerList";
import GameScreen from "./GameScreen";
import ChatBox from "./ChatBox";
import Stats from "./Stats";
import LoadingScreen from "./LoadingScreen";
import { lobbyContext } from "./LobbyContext";
import { useContext } from "react";

function Lobby() {
    const lobbyState = useContext(lobbyContext);

    return lobbyState === null ? (
        <LoadingScreen />
    ) : (
        <div>
            <PlayerList />
            <GameScreen />
            <ChatBox />
            <Stats />
        </div>
    );
}

export default Lobby;
