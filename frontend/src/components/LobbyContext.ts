
import {createContext, useContext} from 'react';
import {type LobbyState} from './LobbyTypes';

export const lobbyContext = createContext<LobbyState | null>(null);


export function useLobbyContext(): LobbyState | null {
    
    if (lobbyContext === null ) {
        throw new Error("Lobby context was called outside of lobby data provider.");
    }
    return useContext(lobbyContext);
}

