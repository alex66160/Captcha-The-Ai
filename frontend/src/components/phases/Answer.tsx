import { useContext, useState, useEffect } from "react";
import { lobbyContext } from "../LobbyContext";
import { publish } from "../StompActions";
import { useParams } from "react-router-dom";
import { getQuestion } from "../LobbyService";

import { type QuestionResponse } from "../LobbyTypes";

type SubmitAnswerRequest = { answer: string };

function Answer() {
    const lobbyState = useContext(lobbyContext);
    if (lobbyState === null) {
        throw new Error("Lobby state was null in answer component");
    }
    const lobbyId = useParams().lobbyId;
    if (lobbyId === undefined) {
        throw new Error("Lobby id was undefined in answer component");
    }
    const [answer, setAnswer] = useState("");
    const [isSubmitted, setIsSubmitted] = useState(false);

    const [questionResponse, setQuestionResponse] =
        useState<QuestionResponse | null>(null);

    useEffect(() => {
        getQuestion(lobbyId).then((response) => {
            setQuestionResponse(response.data);
        });
    }, [lobbyId]);

    const selfPlayer = lobbyState.players.filter((player) => player.isSelf)[0];

    if (selfPlayer.isQuestionWriter && selfPlayer.playerStatus === "ALIVE") {
        return <p>Please wait while players finish answering your question.</p>;
    }

    if (!selfPlayer.isQuestionWriter && selfPlayer.playerStatus === "ALIVE") {
        return questionResponse === null ? null : (
            <div>
                <p>
                    {questionResponse.playerAvatar}{" "}
                    {questionResponse.playerName} wrote:{" "}
                    {questionResponse.question}
                </p>
                <form
                    onSubmit={(event) => {
                        event.preventDefault();
                        publish<SubmitAnswerRequest>(
                            `/app/lobbies/${lobbyId}/answers`,
                            { answer: answer },
                        );
                        setIsSubmitted(true);
                    }}
                >
                    <input
                        type="text"
                        maxLength={100}
                        value={answer}
                        onChange={(event) => setAnswer(event.target.value)}
                    ></input>

                    <button
                        type="submit"
                        disabled={answer.trim() === "" || isSubmitted}
                    >
                        Im finished!
                    </button>
                </form>
            </div>
        );
    }

    if (selfPlayer.playerStatus === "SPECTATOR") {
        return questionResponse === null ? null : (
            <div>
                <p>
                    {questionResponse.playerAvatar}{" "}
                    {questionResponse.playerName} wrote:{" "}
                    {questionResponse.question}
                </p>

                <p>Players are currently answering the question...</p>
            </div>
        );
    }
}

export default Answer;
