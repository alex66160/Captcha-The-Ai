import { useState, createContext, useEffect, type ReactNode } from "react";
import { useLocation } from "react-router-dom";
import { type IMessage, type StompSubscription } from "@stomp/stompjs";
import { subscribeAndWait, getSessionId, publish } from "./StompActions.ts";
import { type LobbyState } from "./LobbyTypes.ts";

const lobbyContext = createContext<LobbyState | null>(null);

function LobbyStateProvider({ children }: { children: ReactNode }) {
    const location = useLocation();
    const lobbyId = location.pathname;

    const [lobbyState, setLobbyState] = useState<LobbyState | null>(null);

    useEffect(() => {
        let lobbyStateSubscription: StompSubscription | null = null;

        (async () => {
            lobbyStateSubscription = await subscribeAndWait(
                `/queue/lobbies/${lobbyId}/state/${getSessionId()}`,
                (message: IMessage) => {
                    setLobbyState(JSON.parse(message.body));
                },
            );

            publish<null>(`app/lobbies/${lobbyId}/state`, null);
        })();

        return () => {
            lobbyStateSubscription?.unsubscribe();
        };
    }, [lobbyId]);

    return (
        <lobbyContext.Provider value={lobbyState}>
            {children}
        </lobbyContext.Provider>
    );
}

export default LobbyStateProvider;
