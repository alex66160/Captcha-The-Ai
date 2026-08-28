
import axios from "axios";

const backendURL = "http://localhost:8080";

export function getEliminatedPlayer(lobbyId: string) {

    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/eliminated-player`);
}


export function getQuestion(lobbyId: string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/question`)
}

export function getAnswers(lobbyId: string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/answers`)
}

export function getVotes(lobbyId :string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/votes`);
}

export function getTiedPlayers(lobbyId: string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/tied-players`);
}

export function getAiPlayer(lobbyId: string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/ai-player`);
}