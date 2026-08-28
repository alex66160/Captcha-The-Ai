import { useContext } from "react";
import { lobbyContext } from "./LobbyContext";

import Intermission from "./phases/Intermission";
import Starting from "./phases/Starting";
import Intro from "./phases/Intro";
import QuestionAnnouncement from "./phases/QuestionAnnouncement";
import Question from "./phases/Question";
import QuestionDisconnect from "./phases/QuestionDisconnect";
import QuestionEmpty from "./phases/QuestionEmpty";
import AnswerAnnouncement from "./phases/AnswerAnnouncement";
import Answer from "./phases/Answer";
import DiscussAnnouncement from "./phases/DiscussAnnouncement";
import Discuss from "./phases/Discuss";
import Voting from "./phases/Voting";
import VotingRestart from "./phases/VotingRestart";
import RevealAnnouncement from "./phases/RevealAnnouncement";
import Reveal from "./phases/Reveal";
import RevealTie from "./phases/RevealTie";
import Elimination from "./phases/Elimination";
import GameResult from "./phases/GameResult";

/**
 * The GameScreen component determines which phase component to show depending on which lobbyPhase it currently is.
 * @author Alex Liu
 */
function GameScreen() {
    const lobbyState = useContext(lobbyContext);

    if (lobbyState === null) {
        throw new Error("Lobby state was null on the game screen.");
    }

    switch (lobbyState.lobbyPhase) {
        case "INTERMISSION":
            return <Intermission />;
            break;
        case "STARTING":
            return <Starting />;
            break;
        case "INTRO":
            return <Intro />;
            break;
        case "QUESTION_ANNOUNCEMENT":
            return <QuestionAnnouncement />;
            break;
        case "QUESTION":
            return <Question />;
            break;
        case "QUESTION_DISCONNECT":
            return <QuestionDisconnect />;
            break;
        case "QUESTION_EMPTY":
            return <QuestionEmpty />;
            break;
        case "ANSWER_ANNOUNCEMENT":
            return <AnswerAnnouncement />;
            break;
        case "ANSWER":
            return <Answer />;
            break;
        case "DISCUSS_ANNOUNCEMENT":
            return <DiscussAnnouncement />;
            break;
        case "DISCUSS":
            return <Discuss />;
            break;
        case "VOTING":
            return <Voting />;
            break;
        case "VOTING_RESTART":
            return <VotingRestart />;
            break;
        case "REVEAL_ANNOUNCEMENT":
            return <RevealAnnouncement />;
            break;
        case "REVEAL":
            return <Reveal />;
            break;
        case "REVEAL_TIE":
            return <RevealTie />;
            break;
        case "ELIMINATION":
            return <Elimination />;
            break;
        // You may notice that these 4 phases map to the same component, and this is because
        // the only thing different is the message displayed in the game result, so I made one component
        // instead of 4 different components for this.
        case "AI_PLAYER_WON":
            return <GameResult />;
            break;
        case "AI_PLAYER_FAILED_TO_RESPOND":
            return <GameResult />;
            break;
        case "HUMAN_PLAYERS_WON":
            return <GameResult />;
            break;
        case "NOT_ENOUGH_PLAYERS":
            return <GameResult />;
            break;
        default:
            throw new Error(
                `Invalid lobby phase on Game Screen: ${lobbyState.lobbyPhase}`,
            );
    }
}

export default GameScreen;
