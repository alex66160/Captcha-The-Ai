import { useEffect, useState, useContext } from "react";
import { useParams } from "react-router-dom";
import { subscribe, publish, getSessionId } from "./StompActions.ts";
import { lobbyContext } from "./LobbyContext.ts";
import { type StompSubscription } from "@stomp/stompjs";

type SendChatMessageCommand = { message: string };
type SentChatMessageBroadcast = { playerName: string; message: string };
// We need separate message types so that our ui can display different styling depending on what kind of message it is.
type MessageType = "ALIVE_PLAYER" | "SPECTATOR_PLAYER" | "ERROR";

type ChatMessage = { message: string; messageType: MessageType };
type ChatCooldownErrorResponse = { timeLeftOnCooldown: number };

const MAX_CHAT_MESSAGES_TO_DISPLAY = 20;

/**
 * The ChatBox component allows players to recieve chat messages and also send chat messages.
 * @author Alex Liu
 */
function ChatBox() {
    const lobbyId = useParams().lobbyId;
    const [messageToSend, setMessageToSend] = useState("");
    const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);

    const lobbyState = useContext(lobbyContext);

    if (lobbyState === null) {
        throw new Error("Lobby state is not supposed to be null");
    }
    // Get the current player status to determine if the player should be able
    // to see spectator chat. Keep in mind spectators can see the alive player chat and spectator chat,
    // but alive player are only able to see alive player chat.
    const currentPlayerStatus = lobbyState.players.filter(
        (player) => player.isSelf,
    )[0].playerStatus;

    // We subscribe to chat message errors so that the backend can send us a message if
    // the player has violated the cooldown and so that the player can know how much longer
    // they have left on their chat cooldown before they can send another message.
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
                // This just limits all messages displayed in the chatbox to 20.
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

    // This represents the regular alive chat that all players are subscribed to in a lobby.
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
                // This just limits all messages displayed in the chatbox to 20.
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

    // This represents the spectator chat subscription if the player is a spectator.
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
                    // This just limits all messages displayed in the chatbox to 20.
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
