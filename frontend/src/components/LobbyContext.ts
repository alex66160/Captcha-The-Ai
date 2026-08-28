
import {createContext} from 'react';
import {type LobbyState} from './LobbyTypes';

// We have to keep lobbyContext is a separate typescript file because es lint won't let me place
// it inside the LobbyContextProvider component as it'll cause fast refresh to break.
export const lobbyContext = createContext<LobbyState | null>(null);




