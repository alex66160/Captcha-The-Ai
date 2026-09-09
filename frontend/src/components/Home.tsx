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
        <div
            className="
        select-none 
        overflow-hidden
         h-screen 
         w-screen"
        >
            {message && (
                <div>
                    <button onClick={() => setMessage(null)}></button>
                    <p>{message}</p>
                </div>
            )}
            <div
                className="
            flex 
            h-screen
            w-screen
            flex-col 
            items-center
            justify-center"
            >
                <p
                    className="
                text-[clamp(1rem,8vw,6rem)] 
                title-animation 
                text-white 
                m-10 "
                >
                    Captcha The Ai
                </p>
                <button
                    className="
                    text-[clamp(.75rem,2vw,1.25rem)]
                    text-white 
                    border-white 
                    border-2 
                    p-2 
                    m-2 
                    w-[clamp(150px,20vw,300px)]
                    aspect-[5/1]
                    rounded-sm
                    md:rounded-md 
                    
                    bg-blue-500 
                    hover:scale-[1.05] 
                    button-animation
                    active:scale-[.95]
                    transition"
                    onClick={() => {
                        connect(joinRandomLobby);
                    }}
                >
                    Join Lobby
                </button>
                <button
                    className="
                    text-[clamp(.75rem,2vw,1.25rem)]
                    text-white 
                    border-white 
                    border-2 
                    p-2 
                    m-2 
                    w-[clamp(150px,20vw,300px)]
                    aspect-[5/1]
                    rounded-sm
                    md:rounded-md 
                    
                    bg-blue-500 
                    hover:scale-[1.05] 
                    button-animation
                    active:scale-[.95]
                    transition"
                    onClick={() => {
                        setDisplayJoinLobbyByIdForm(true);
                        setDisplayCreateLobbyForm(false);
                        setLobbyIdToJoin("");
                        setLobbyPassword("");
                    }}
                >
                    Join Lobby By ID
                </button>
                <button
                    className="
                    text-[clamp(.75rem,2vw,1.25rem)]
                    text-white 
                    border-white 
                    border-2 
                    p-2 
                    m-2 
                    w-[clamp(150px,20vw,300px)]
                    aspect-[5/1]
                    rounded-sm
                    md:rounded-md 
                    
                    bg-blue-500 
                    hover:scale-[1.05] 
                    button-animation
                    active:scale-[.95]
                    transition"
                    onClick={() => {
                        setDisplayCreateLobbyForm(true);
                        setDisplayJoinLobbyByIdForm(false);
                        setLobbyIdToJoin("");
                        setLobbyPassword("");
                    }}
                >
                    Create Lobby
                </button>
            </div>

            {displayJoinLobbyByIdForm && (
                <div className="fixed flex inset-0 z-50 justify-center items-center bg-black/50">
                    <form
                        className="popup-animation flex flex-col justify-center items-center text-white text-xl border-white border-2 rounded-lg bg-blue-500 w-1/4 aspect-[2/1] relative"
                        onSubmit={(event) => {
                            event.preventDefault();
                            connect(joinLobbyById);
                            setDisplayJoinLobbyByIdForm(false);
                        }}
                    >
                        <p className="text-2xl">Join Lobby By ID</p>
                        <button
                            className="absolute top-2 right-4"
                            onClick={() => {
                                setDisplayJoinLobbyByIdForm(false);
                            }}
                        >
                            x
                        </button>
                        <input
                            className="border-2 rounded-md p-1 m-1 outline-none"
                            type="text"
                            value={lobbyIdToJoin}
                            placeholder="Enter Lobby ID"
                            onChange={(event) =>
                                setLobbyIdToJoin(event.target.value)
                            }
                        ></input>
                        <input
                            className="border-2 rounded-md p-1 m-1 focus:outline-none focus:border-blue-200"
                            type="text"
                            value={lobbyPassword}
                            placeholder="Enter Lobby Password"
                            onChange={(event) =>
                                setLobbyPassword(event.target.value)
                            }
                        ></input>
                        <button
                            className="
                    text-xl 
                    text-white 
                    border-white 
                    border-2 
                    p-2
                    m-2
                    w-1/3 
                    aspect-[5/1] 
               
                    rounded-lg 
                    bg-blue-500 
                    hover:bg-blue-300 
                    hover:scale-[1.05] 
                    button-animation
                    active:scale-[.95]
                    transition"
                            type="submit"
                        >
                            Submit
                        </button>
                    </form>
                </div>
            )}

            {displayCreateLobbyForm && (
                <form
                    onSubmit={(event) => {
                        event.preventDefault();
                        connect(createLobby);
                        setDisplayCreateLobbyForm(false);
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
