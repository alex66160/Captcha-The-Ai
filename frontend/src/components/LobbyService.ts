
import axios from "axios";



export function getEliminatedPlayer(lobbyId: string) {

    return axios.get(`/api/lobbies/${lobbyId}/eliminated-player`);
}