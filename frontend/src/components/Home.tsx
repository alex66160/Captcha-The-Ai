import { useState } from "react";
import {
    subscribeAndWait,
    publish,
    getSessionId,
    connect,
} from "./StompActions.ts";
import { useNavigate, useLocation } from "react-router-dom";

type LobbyIdResponse = { lobbyId: number };
type JoinLobbyByIdRequest = { lobbyPassword: string | null };
type CreateLobbyRequest = { lobbyPassword: string | null };

type LobbyErrorType =
    | "LOBBY_FULL"
    | "LOBBY_NOT_FOUND"
    | "LOBBY_INCORRECT_PASSWORD";
type LobbyErrorTypeResponse = { lobbyErrorType: LobbyErrorType };

function Home() {
    const [lobbyIdToJoin, setLobbyIdToJoin] = useState("");
    const [lobbyPassword, setLobbyPassword] = useState("");

    const [message, setMessage] = useState<string | null>(null);

    const [displayJoinLobbyByIdForm, setDisplayJoinLobbyByIdForm] =
        useState(false);
    const [displayCreateLobbyForm, setDisplayCreateLobbyForm] = useState(false);

    const navigate = useNavigate();
    const location = useLocation();

    const redirectMessage: string | null = location.state;
    if (redirectMessage !== null) {
        setMessage(redirectMessage);
    }

    const joinRandomLobby = async () => {
        const lobbyIdSubscription = await subscribeAndWait(
            `/queue/lobbies/join/${getSessionId()}`,
            (message) => {
                const lobbyIdResponse: LobbyIdResponse = JSON.parse(
                    message.body,
                );
                navigate(`/${lobbyIdResponse.lobbyId}`);
                lobbyIdSubscription.unsubscribe();
            },
        );

        publish<null>("/app/lobbies/join", null);
    };

    const joinLobbyById = async () => {
        const lobbyIdSubscription = await subscribeAndWait(
            `/queue/lobbies/join/${getSessionId()}`,
            (message) => {
                const lobbyIdResponse: LobbyIdResponse = JSON.parse(
                    message.body,
                );
                navigate(`/${lobbyIdResponse.lobbyId}`);
                lobbyIdSubscription.unsubscribe();
            },
        );

        const lobbyErrorSubscription = await subscribeAndWait(
            `/queue/lobbies/errors/${getSessionId()}`,
            (message) => {
                const lobbyErrorTypeResponse: LobbyErrorTypeResponse =
                    JSON.parse(message.body);
                const lobbyErrorType = lobbyErrorTypeResponse.lobbyErrorType;

                switch (lobbyErrorType) {
                    case "LOBBY_FULL":
                        setMessage("The lobby you tried to join is full.");
                        break;
                    case "LOBBY_INCORRECT_PASSWORD":
                        setMessage("Incorrect password entered.");
                        break;
                    case "LOBBY_NOT_FOUND":
                        setMessage("Lobby was not found.");
                        break;
                }

                lobbyErrorSubscription.unsubscribe();
            },
        );

        const joinLobbyByIdRequest: JoinLobbyByIdRequest = { lobbyPassword };
        publish<JoinLobbyByIdRequest>(
            `/app/lobbies/${lobbyIdToJoin}/join`,
            joinLobbyByIdRequest,
        );
    };

    const createLobby = async () => {
        const lobbyIdSubscription = await subscribeAndWait(
            `/queue/lobbies/join/${getSessionId()}`,
            (message) => {
                const lobbyIdResponse: LobbyIdResponse = JSON.parse(
                    message.body,
                );
                navigate(`/${lobbyIdResponse.lobbyId}`);
                lobbyIdSubscription.unsubscribe();
            },
        );
        const createLobbyRequest: CreateLobbyRequest = { lobbyPassword };
        publish<CreateLobbyRequest>("/app/lobbies/create", createLobbyRequest);
    };

    return (
        <div>
            {message} &&
            <div>
                <button onClick={() => setMessage(null)}></button>
                <p>{message}</p>
            </div>
            <button
                onClick={() => {
                    connect(joinRandomLobby);
                }}
            ></button>
            <button onClick={() => setDisplayJoinLobbyByIdForm(true)}></button>
            {displayJoinLobbyByIdForm} &&{" "}
            <form
                onSubmit={() => {
                    connect(joinLobbyById);
                }}
            >
                <input
                    type="text"
                    value={lobbyIdToJoin}
                    onChange={(event) => setLobbyIdToJoin(event.target.value)}
                ></input>

                <input
                    type="password"
                    value={lobbyPassword}
                    onChange={(event) => setLobbyPassword(event.target.value)}
                ></input>
                <button type="submit">Submit</button>
            </form>
            <button onClick={() => setDisplayCreateLobbyForm(true)}></button>
            {displayCreateLobbyForm} &&{" "}
            <form
                onSubmit={() => {
                    connect(createLobby);
                }}
            >
                <input
                    type="password"
                    value={lobbyPassword}
                    onChange={(event) => setLobbyPassword(event.target.value)}
                ></input>
            </form>
        </div>
    );
}

export default Home;
