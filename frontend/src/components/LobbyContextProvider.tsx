import { useState, useEffect, type ReactNode } from "react";
import { useParams } from "react-router-dom";
import { type IMessage, type StompSubscription } from "@stomp/stompjs";
import { subscribe, getSessionId, publish } from "./StompActions.ts";
import { type LobbyState } from "./LobbyTypes.ts";
import { lobbyContext } from "./LobbyContext.ts";

function LobbyContextProvider({ children }: { children: ReactNode }) {
    const lobbyId = useParams().lobbyId;

    const [lobbyState, setLobbyState] = useState<LobbyState | null>(null);

    useEffect(() => {
        let lobbyStateSubscription: StompSubscription | null = null;

        lobbyStateSubscription = subscribe(
            `/queue/lobbies/${lobbyId}/state/${getSessionId()}`,
            (message: IMessage) => {
                console.log("STATE SUSCRIPTION WENT THROUGH");
                setLobbyState(JSON.parse(message.body));
            },
        );

        publish<null>(`/app/lobbies/${lobbyId}/state`, null);
        console.log("STATE PUBLISH WENT THROUGH");

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

export default LobbyContextProvider;
