import { useState, createContext } from "react";

type LobbyPhase =
    | "INTERMISSION"
    | "STARTING"
    | "INTRO"
    | "QUESTION_ANNOUNCEMENT"
    | "QUESTION"
    | "QUESTION_DISCONNECT"
    | "QUESTION_EMPTY"
    | "ANSWER_ANNOUNCEMENT"
    | "ANSWER"
    | "DISCUSS_ANNOUNCEMENT"
    | "DISCUSS"
    | "VOTING"
    | "VOTING_RESTART"
    | "REVEAL_ANNOUNCEMENT"
    | "REVEAL"
    | "REVEAL_TIE"
    | "ELIMINATION"
    | "AI_PLAYER_WON"
    | "AI_PLAYER_FAILED_TO_RESPOND"
    | "HUMAN_PLAYERS_WON"
    | "NOT_ENOUGH_PLAYERS";

type PlayerAvatar =
    | "MONKEY"
    | "DOG"
    | "WOLF"
    | "FOX"
    | "RACCOON"
    | "CAT"
    | "LION"
    | "TIGER"
    | "COW"
    | "PIG"
    | "MOUSE"
    | "HAMSTER"
    | "RABBIT"
    | "BEAR"
    | "PANDA"
    | "BIRD"
    | "PENGUIN"
    | "EAGLE"
    | "DUCK"
    | "FROG"
    | "TURTLE"
    | "SNAKE"
    | "WHALE"
    | "DOLPHIN"
    | "SEAL"
    | "SHARK"
    | "OCTOPUS"
    | "CRAB"
    | "SPECTATOR"
    | "HIDDEN";

type PlayerState = {
    playerName: string;
    playerAvatar: PlayerAvatar;
    isQuestionWriter: boolean;
    isSelf: boolean;
};

type LobbyState = {
    lobbyPhase: LobbyPhase;
    phaseEndTime: string;
    roundCount: number;
    players: PlayerState[];
};

const lobbyContext = createContext(null);

function LobbyStateProvider({ children }) {
    const [lobbyState, setLobbyState] = useState<LobbyState | null>(null);
}

export default LobbyStateProvider;
