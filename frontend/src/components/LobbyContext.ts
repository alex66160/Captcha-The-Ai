
import {createContext} from 'react';
import {type LobbyState} from './LobbyTypes';

export const lobbyContext = createContext<LobbyState | null>(null);




