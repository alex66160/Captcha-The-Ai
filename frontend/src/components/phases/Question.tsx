import { lobbyContext } from "../LobbyContext";
import { useContext, useState } from "react";
import { useParams } from "react-router-dom";
import { publish } from "../StompActions";

type SubmitQuestionRequest = { question: string };

/**
 * The Question component allows the question writer to submit a question.
 * @author Alex Liu
 */
function Question() {
    const lobbyId = useParams().lobbyId;
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error("Lobby state is null in Question phase.");
    }
    const [isSubmitted, setIsSubmitted] = useState(false);
    const [question, setQuestion] = useState("");
    // We get both the question writer and the self player identity so that we know what message to display.
    const questionWriter = lobbyState.players.filter(
        (player) => player.isQuestionWriter,
    )[0];

    const selfPlayer = lobbyState.players.filter((player) => player.isSelf)[0];

    if (selfPlayer.isQuestionWriter && selfPlayer.playerStatus === "ALIVE") {
        return (
            <div>
                <form
                    onSubmit={(event) => {
                        event.preventDefault();
                        publish<SubmitQuestionRequest>(
                            `/app/lobbies/${lobbyId}/question`,
                            { question: question },
                        );
                        setIsSubmitted(true);
                    }}
                >
                    <input
                        type="text"
                        maxLength={100}
                        value={question}
                        onChange={(event) => setQuestion(event.target.value)}
                    ></input>

                    <button
                        type="submit"
                        disabled={question.trim() === "" || isSubmitted}
                    >
                        Im finished!
                    </button>
                </form>
            </div>
        );
    }

    if (!selfPlayer.isQuestionWriter && selfPlayer.playerStatus === "ALIVE") {
        return (
            <p>
                {" "}
                {questionWriter.playerName} is currently writing the question,
                get ready to answer it soon!
            </p>
        );
    }

    if (selfPlayer.playerStatus === "SPECTATOR") {
        return (
            <p>
                {" "}
                {questionWriter.playerName} is currently writing the question.
            </p>
        );
    }
}

export default Question;
