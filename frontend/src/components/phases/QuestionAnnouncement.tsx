import { lobbyContext } from "../LobbyContext";
import { useContext } from "react";

/**
 * The QuestionAnnouncement component shows a message to let players know who is writing the question.
 * @author Alex Liu
 */
function QuestionAnnouncement() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error("Lobby state was null in Question Announcement phase.");
    }

    const questionWriter = lobbyState.players.filter(
        (player) => player.isQuestionWriter,
    )[0];
    return (
        <p>{questionWriter.playerName} will be writing this rounds question.</p>
    );
}

export default QuestionAnnouncement;
