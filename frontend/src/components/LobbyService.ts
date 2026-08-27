
import axios from "axios";



export function getEliminatedPlayer(lobbyId: string) {

    return axios.get(`/api/lobbies/${lobbyId}/eliminated-player`);
}


export function getQuestion(lobbyId: string) {
    return axios.get(`/api/lobbies/${lobbyId}/question`)
}

export function getAnswers(lobbyId: string) {
    return axios.get(`/api/lobbies/${lobbyId}/answers`)
}

export function getVotes(lobbyId :string) {
    return axios.get(`/api/lobbies/${lobbyId}/votes`);
}

export function getTiedPlayers(lobbyId: string) {
    return axios.get(`/api/lobbies/${lobbyId}/tied-players`);
}

export function getAiPlayer(lobbyId: string) {
    return axios.get(`/api/lobbies/${lobbyId}/ai-player`);
}