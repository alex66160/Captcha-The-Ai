import PlayerList from "./PlayerList";
import GameScreen from "./GameScreen";
import ChatBox from "./ChatBox";
import Stats from "./Stats";
import LoadingScreen from "./LoadingScreen";
import { useLobbyContext } from "./LobbyContext";

function Lobby() {
    const lobbyState = useLobbyContext();

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
