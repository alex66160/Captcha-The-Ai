export type LobbyPhase =
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

export type PlayerAvatar =
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

export type PlayerState = {
    playerName: string;
    playerAvatar: PlayerAvatar;
    isQuestionWriter: boolean;
    isSelf: boolean;
};

export type LobbyState = {
    lobbyPhase: LobbyPhase;
    phaseEndTime: string;
    roundCount: number;
    players: PlayerState[];
};