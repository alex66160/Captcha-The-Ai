import { useState } from "react";
import { subscribe, publish, getSessionId, connect } from "./StompActions.ts";
import { useNavigate, useLocation } from "react-router-dom";

type LobbyIdResponse = { lobbyId: number };
type JoinLobbyByIdRequest = { lobbyPassword: string | null };
type CreateLobbyRequest = { lobbyPassword: string | null };

type LobbyErrorType =
    | "LOBBY_FULL"
    | "LOBBY_NOT_FOUND"
    | "LOBBY_INCORRECT_PASSWORD";
type LobbyErrorTypeResponse = { lobbyErrorType: LobbyErrorType };

/**
 * The Home component allows players to join a random lobby, join a lobby by its lobby id, and create a lobby.
 * @author Alex Liu
 */
function Home() {
    const location = useLocation();
    const [lobbyIdToJoin, setLobbyIdToJoin] = useState("");
    const [lobbyPassword, setLobbyPassword] = useState("");

    // location.state represents the initial message to display when players get kicked and are navigated
    // back to the Home component. Otherwise location.state will just be null.
    const [message, setMessage] = useState<string | null>(location.state);

    const [displayJoinLobbyByIdForm, setDisplayJoinLobbyByIdForm] =
        useState(false);
    const [displayCreateLobbyForm, setDisplayCreateLobbyForm] = useState(false);

    const navigate = useNavigate();

    // The next three arrow functions will follow a similar pattern of setting up a subscribe for
    // the lobbyId response, and navigating to the lobbyId page once we recieve it after we make a publish
    // to join/create the lobby.

    // The reason we use stomp instead of restapi here is so that the backend
    // can extract the sessionId from the header to know who sent the join/create instead of us having to
    // manually insert the sessionId or needing to use some other kind of authentication.

    const joinRandomLobby = () => {
        const lobbyIdSubscription = subscribe(
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

    const joinLobbyById = () => {
        const lobbyIdSubscription = subscribe(
            `/queue/lobbies/join/${getSessionId()}`,
            (message) => {
                const lobbyIdResponse: LobbyIdResponse = JSON.parse(
                    message.body,
                );
                navigate(`/${lobbyIdResponse.lobbyId}`);
                lobbyIdSubscription.unsubscribe();
            },
        );
        // Since the user could join a lobby thats full, type an incorrect password, or enter
        // a lobby id for a lobby that doesn't exist, we need to be able to recieve errors
        // from the backend when it happens and let the user know.
        const lobbyErrorSubscription = subscribe(
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

    const createLobby = () => {
        const lobbyIdSubscription = subscribe(
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
            {message && (
                <div>
                    <button onClick={() => setMessage(null)}></button>
                    <p>{message}</p>
                </div>
            )}
            <button
                onClick={() => {
                    connect(joinRandomLobby);
                }}
            >
                Join random lobby button
            </button>
            <button
                onClick={() => {
                    setDisplayJoinLobbyByIdForm(true);
                    setDisplayCreateLobbyForm(false);
                    setLobbyIdToJoin("");
                    setLobbyPassword("");
                }}
            >
                Join lobby by id button
            </button>
            {displayJoinLobbyByIdForm && (
                <form
                    onSubmit={(event) => {
                        event.preventDefault();
                        connect(joinLobbyById);
                    }}
                >
                    <button
                        onClick={() => {
                            setDisplayJoinLobbyByIdForm(false);
                        }}
                    >
                        Click to close join lobby by id form
                    </button>
                    <input
                        type="text"
                        value={lobbyIdToJoin}
                        onChange={(event) =>
                            setLobbyIdToJoin(event.target.value)
                        }
                    ></input>

                    <input
                        type="password"
                        value={lobbyPassword}
                        onChange={(event) =>
                            setLobbyPassword(event.target.value)
                        }
                    ></input>
                    <button type="submit">Submit</button>
                </form>
            )}

            <button
                onClick={() => {
                    setDisplayCreateLobbyForm(true);
                    setDisplayJoinLobbyByIdForm(false);
                    setLobbyIdToJoin("");
                    setLobbyPassword("");
                }}
            >
                Create lobby button
            </button>
            {displayCreateLobbyForm && (
                <form
                    onSubmit={(event) => {
                        event.preventDefault();
                        connect(createLobby);
                    }}
                >
                    <button
                        onClick={() => {
                            setDisplayCreateLobbyForm(false);
                        }}
                    >
                        Click to close create lobby form
                    </button>
                    <input
                        type="password"
                        value={lobbyPassword}
                        onChange={(event) =>
                            setLobbyPassword(event.target.value)
                        }
                    ></input>
                    <button type="submit">Submit</button>
                </form>
            )}
        </div>
    );
}

export default Home;
