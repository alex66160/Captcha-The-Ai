import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { subscribe, publish, getSessionId } from "./StompActions.ts";
import { useLobbyContext } from "./LobbyContext.ts";
import { type StompSubscription } from "@stomp/stompjs";

type SendChatMessageCommand = { message: string };
type SentChatMessageBroadcast = { playerName: string; message: string };
type MessageType = "ALIVE_PLAYER" | "SPECTATOR_PLAYER" | "ERROR";

type ChatMessage = { message: string; messageType: MessageType };
type ChatCooldownErrorResponse = { timeLeftOnCooldown: number };

const MAX_CHAT_MESSAGES_TO_DISPLAY = 20;

function ChatBox() {
    const lobbyId = useParams().lobbyId;
    const [messageToSend, setMessageToSend] = useState("");
    const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);

    const lobbyState = useLobbyContext();

    if (lobbyState === null) {
        throw new Error("Lobby state is not supposed to be null");
    }
    const currentPlayerStatus = lobbyState.players.filter(
        (player) => player.isSelf,
    )[0].playerStatus;

    useEffect(() => {
        const chatErrorSubscription = subscribe(
            `/queue/lobbies/${lobbyId}/chat-messages/errors/${getSessionId()}`,
            (message) => {
                const chatCooldownErrorResponse: ChatCooldownErrorResponse =
                    JSON.parse(message.body);

                const chatMessage: ChatMessage = {
                    message: `Please wait ${chatCooldownErrorResponse.timeLeftOnCooldown} seconds before sending another message.`,
                    messageType: "ERROR",
                };
                setChatMessages((prev) => {
                    if (prev.length === MAX_CHAT_MESSAGES_TO_DISPLAY) {
                        return [...prev.slice(1), chatMessage];
                    } else {
                        return [...prev, chatMessage];
                    }
                });
            },
        );

        return () => {
            chatErrorSubscription.unsubscribe();
        };
    }, [lobbyId]);

    useEffect(() => {
        const chatSubscription = subscribe(
            `/topic/lobbies/${lobbyId}/chat-messages`,
            (message) => {
                const sentChatMessageBroadcast: SentChatMessageBroadcast =
                    JSON.parse(message.body);

                const chatMessage: ChatMessage = {
                    message: `${sentChatMessageBroadcast.playerName}: ${sentChatMessageBroadcast.message}`,
                    messageType: "ALIVE_PLAYER",
                };
                setChatMessages((prev) => {
                    if (prev.length === MAX_CHAT_MESSAGES_TO_DISPLAY) {
                        return [...prev.slice(1), chatMessage];
                    } else {
                        return [...prev, chatMessage];
                    }
                });
            },
        );

        return () => {
            chatSubscription.unsubscribe();
        };
    }, [lobbyId]);

    useEffect(() => {
        let spectatorChatSubscription: StompSubscription | null = null;
        if (currentPlayerStatus === "SPECTATOR") {
            spectatorChatSubscription = subscribe(
                `/topic/lobbies/${lobbyId}/spectator/chat-messages`,
                (message) => {
                    const sentChatMessageBroadcast: SentChatMessageBroadcast =
                        JSON.parse(message.body);
                    const chatMessage: ChatMessage = {
                        message: `${sentChatMessageBroadcast.playerName}: ${sentChatMessageBroadcast.message}`,
                        messageType: "SPECTATOR_PLAYER",
                    };
                    setChatMessages((prev) => {
                        if (prev.length === MAX_CHAT_MESSAGES_TO_DISPLAY) {
                            return [...prev.slice(1), chatMessage];
                        } else {
                            return [...prev, chatMessage];
                        }
                    });
                },
            );
        }

        return () => {
            spectatorChatSubscription?.unsubscribe();
        };
    }, [currentPlayerStatus, lobbyId]);

    return (
        <div>
            {chatMessages.map((chatMessage) => (
                <p>{chatMessage.message + " " + chatMessage.messageType}</p>
            ))}

            <form
                onSubmit={(event) => {
                    event.preventDefault();
                    publish<SendChatMessageCommand>(
                        `/app/lobbies/${lobbyId}/chat-messages`,
                        { message: messageToSend },
                    );
                    setMessageToSend("");
                }}
            >
                <input
                    type="text"
                    value={messageToSend}
                    maxLength={100}
                    disabled={
                        lobbyState.lobbyPhase === "INTERMISSION" ||
                        lobbyState.lobbyPhase === "STARTING"
                    }
                    onChange={(event) => setMessageToSend(event.target.value)}
                />
                <p>{messageToSend.length}</p>
            </form>
        </div>
    );
}

export default ChatBox;
