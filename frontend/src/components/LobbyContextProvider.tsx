import { useState, useEffect, type ReactNode } from "react";
import { useParams } from "react-router-dom";
import { type IMessage, type StompSubscription } from "@stomp/stompjs";
import { subscribe, getSessionId, publish } from "./StompActions.ts";
import { type LobbyState } from "./LobbyTypes.ts";
import { lobbyContext } from "./LobbyContext.ts";

/**
 * The LobbyContextProvider fetches the lobbyState and stores it, and also allows
 * child components to access the lobbyState using lobbyContext as the container.
 * @author Alex Liu
 * @param children the components that need to use the LobbyState
 */
function LobbyContextProvider({ children }: { children: ReactNode }) {
    const lobbyId = useParams().lobbyId;

    const [lobbyState, setLobbyState] = useState<LobbyState | null>(null);

    // Subscribe to listen to lobby state and make an initial publish to fetch
    // the lobby state so that the players dont have a blank screen until the
    // backend broadcasts the lobby state later.
    useEffect(() => {
        let lobbyStateSubscription: StompSubscription | null = null;

        lobbyStateSubscription = subscribe(
            `/queue/lobbies/${lobbyId}/state/${getSessionId()}`,
            (message: IMessage) => {
                setLobbyState(JSON.parse(message.body));
            },
        );

        publish<null>(`/app/lobbies/${lobbyId}/state`, null);

        return () => {
            lobbyStateSubscription?.unsubscribe();
        };
    }, [lobbyId]);

    return (
        /* We need to wrap the child components in the lobbyContext.Provider and provide 
        the value in the container as the lobbyState so that the children can access the lobbyState
        using the lobbyContext with useContext.*/
        <lobbyContext.Provider value={lobbyState}>
            {children}
        </lobbyContext.Provider>
    );
}

export default LobbyContextProvider;
