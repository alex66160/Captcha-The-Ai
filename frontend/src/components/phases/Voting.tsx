import { useContext, useState, useEffect } from "react";
import { lobbyContext } from "../LobbyContext";
import { useParams } from "react-router-dom";
import { type QuestionResponse, type AnswersResponse } from "../LobbyTypes";
import { getQuestion, getAnswers } from "../LobbyService";
import { publish } from "../StompActions";

type SubmitVoteRequest = { voteTargetName: string };

function Voting() {
    const lobbyState = useContext(lobbyContext);
    const [isSubmitted, setIsSubmitted] = useState(false);
    if (lobbyState === null) {
        throw new Error("Lobby state was null in discuss phase");
    }
    const lobbyId = useParams().lobbyId;
    if (lobbyId === undefined) {
        throw new Error("Lobby id was undefined in answer component");
    }
    const [questionResponse, setQuestionResponse] =
        useState<QuestionResponse | null>(null);

    useEffect(() => {
        getQuestion(lobbyId).then((response) => {
            setQuestionResponse(response.data);
        });
    }, [lobbyId]);

    const [answersResponse, setAnswersResponse] =
        useState<AnswersResponse | null>(null);

    useEffect(() => {
        getAnswers(lobbyId).then((response) =>
            setAnswersResponse(response.data),
        );
    }, [lobbyId]);

    return questionResponse === null || answersResponse === null ? null : (
        <div>
            <button
                disabled={isSubmitted}
                onClick={() => {
                    publish<SubmitVoteRequest>(
                        `/app/lobbies/${lobbyId}/votes`,
                        {
                            voteTargetName: questionResponse.playerName,
                        },
                    );
                    setIsSubmitted(true);
                }}
            >
                {questionResponse.playerAvatar} {questionResponse.playerName}{" "}
                wrote: {questionResponse.question}
            </button>
            {answersResponse.answers.map((answerResponse, index) => (
                <button
                    disabled={isSubmitted}
                    key={index}
                    onClick={() => {
                        publish<SubmitVoteRequest>(
                            `/app/lobbies/${lobbyId}/votes`,
                            {
                                voteTargetName: answerResponse.playerName,
                            },
                        );
                        setIsSubmitted(true);
                    }}
                >
                    {answerResponse.playerAvatar} {answerResponse.playerName}{" "}
                    wrote: {answerResponse.answer}
                </button>
            ))}
        </div>
    );
}

export default Voting;
