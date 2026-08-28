
import axios from "axios";

const backendURL = "http://localhost:8080";

// You might wonder why we are even using restapi requests here for getting lobby info
// rather than having it broadcasted over from the backend with stomp, and this is so
// that spectators joining mid game wont miss sent over broadcasts and just see an empty
// screen when they join.

// I also didn't make the LobbyState contain all these extra lobby info below as they are 
// phase specific and will be null most of the time, so I made LobbyState contain information
// that applied to basically all the phases (such as round count or the player list).


/**
 * The getEliminatedPlayer function returns the eliminated player from a given lobby.
 * @param lobbyId the lobbyId to get the eliminated player from
 * @returns the eliminated player
 */
export function getEliminatedPlayer(lobbyId: string) {

    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/eliminated-player`);
}

/**
 * The getQuestion function returns the question from a given lobby.
 * @param lobbyId the lobbyId to get the question from
 * @returns the question
 */
export function getQuestion(lobbyId: string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/question`)
}

/**
 * The getAnswers function returns the answers from a given lobby.
 * @param lobbyId the lobbyId to get the answers from
 * @returns the answers
 */
export function getAnswers(lobbyId: string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/answers`)
}
/**
 * The getVotes function returns the votes from a given lobby.
 * @param lobbyId the lobbyId to get the votes from
 * @returns the votes
 */
export function getVotes(lobbyId :string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/votes`);
}
/**
 * The getTiedPlayers function returns the tied players from a given lobby.
 * @param lobbyId the lobbyId to get the tied players from
 * @returns the tied players
 */
export function getTiedPlayers(lobbyId: string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/tied-players`);
}
/**
 * The getAiPlayer function returns the ai player from a given lobby.
 * @param lobbyId the lobbyId to get the ai player from
 * @returns the ai player
 */
export function getAiPlayer(lobbyId: string) {
    return axios.get(`${backendURL}/api/lobbies/${lobbyId}/ai-player`);
}