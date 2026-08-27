import { useContext, useState, useEffect } from "react";
import { lobbyContext } from "../LobbyContext";
import { useParams } from "react-router-dom";
import { type QuestionResponse, type AnswersResponse } from "../LobbyTypes";
import { getQuestion, getAnswers } from "../LobbyService";

function Discuss() {
    const lobbyState = useContext(lobbyContext);
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
            <button>
                {questionResponse.playerAvatar} {questionResponse.playerName}{" "}
                wrote: {questionResponse.question}
            </button>
            {answersResponse.answers.map((answerResponse) => (
                <button key={answerResponse.playerName}>
                    {answerResponse.playerAvatar} {answerResponse.playerName}{" "}
                    wrote: {answerResponse.answer}
                </button>
            ))}
        </div>
    );
}

export default Discuss;
