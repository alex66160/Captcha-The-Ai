import { useContext, useState } from "react";
import { lobbyContext } from "../LobbyContext";
import { publish } from "../StompActions";
import { useParams } from "react-router-dom";

type SubmitAnswerRequest = { answer: string };

function Answer() {
    const lobbyState = useContext(lobbyContext);
    const lobbyId = useParams().lobbyId;
    const [answer, setAnswer] = useState("");
    const [isSubmitted, setIsSubmitted] = useState(false);
    if (lobbyState === null) {
        throw new Error("Lobby state was null in answer component");
    }
    const selfPlayer = lobbyState.players.filter((player) => player.isSelf)[0];

    if (selfPlayer.isQuestionWriter && selfPlayer.playerStatus === "ALIVE") {
        return <p>Please wait while players finish answering your question.</p>;
    }

    if (!selfPlayer.isQuestionWriter && selfPlayer.playerStatus === "ALIVE") {
        return (
            <div>
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
        return <p>Players are currently answering the question...</p>;
    }
}

export default Answer;
