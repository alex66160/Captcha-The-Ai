import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'


type ThrowawayUUIDRequest = {throwawayUUID: `${string}-${string}-${string}-${string}-${string}`};
type SessionIdResponse = {sessionId: string};



/** The stomp client that holds the actual connection and provides stomp actions. */
let client: Client | null = null;

/** The users sessionId for the current connected session. */
let sessionId: string | null = null;

/**
 * The connect function connects to the backend, and then runs a given callback.
 * @author Alex Liu
 * @param callback the behavior to run after connecting
 * @throws Error if attempting to connect while already connected
 */
export function connect(callback: () => void): void {
    if (client !== null) {
        throw new Error("Attempted to connect while already connected.");  
    }

    client = new Client({
        // Backend url for the stomp websocket connection
        brokerURL: "ws://localhost:8080/ws",
        onConnect: () => {
            // Because the backend does NOT send the sessionId on the connect frame, we need to get it ourselves from the backend.
            // Make a client side UUID so that the backend has somewhere to send the sessionId, and we need to use a stomp SEND so
            // that the backend can get the sessionId from the header with a header accessor and send it back to the frontend.
            const throwawayUUID = crypto.randomUUID();
            subscribe(`/queue/session-id/${throwawayUUID}`, (message: IMessage) => {
                const sessionIdResponse: SessionIdResponse = JSON.parse(message.body);
                sessionId = sessionIdResponse.sessionId;
                // Run our callback once we have the sessionId since it depends on it.
                callback();
            })

            publish<ThrowawayUUIDRequest>("/app/session-id", {throwawayUUID} );



          
            
        }
    });

    client.activate();
}

/**
 * The subscribe function allows the user to subscribe to a destination and provide a callback to run when messages are
 * published to that destination. The user is responsible for storing the StompSubscription when calling this function, so
 * that they can unsubscribe.
 * @author Alex Liu
 * @param destination the destination to subscribe to
 * @param callback the behavior to run whenever something is published to that destination
 * @returns the subscription that was created
 * @throws Error if attempting to subscribe without a connection
 */
export function subscribe(destination: string, callback: (message: IMessage) => void): StompSubscription {

    if (client === null) {
        throw new Error("Attempted to subscribe to destination: " + destination + " while disconnected.");
        
    }
    
    return client.subscribe(destination, callback);
}


/**
 * The publish function allows the user to publish data to a destination.
 * @author Alex Liu
 * @template T the type of the data thats in the message to be published
 * @param destination the destination to publish to
 * @param data the actual data being sent over thats wrapped in the message
 * @throws Error if attempting to publish without a connection
 */
export function publish<T>(destination: string, data: T): void {

    if (client === null) {
        throw new Error("Attempted to publish to destination: " + destination + " while disconnected.");
    }
    // Turn our data into a json string before publishing
    console.log("PUBLSIHING" + destination);
    client.publish({destination: destination, body: JSON.stringify(data)});
}

/**
 * The disconnect function allows the user to disconnect from the backend.
 * @author Alex Liu
 * @throws Error if attempting to disconnect while already disconnected
 */
export function disconnect(): void {
    if (client === null) {
        throw new Error("Attempted to disconnect while already disconnected.");  
    }
    // We first set client and sessionId to null before deactivating so that the user can connect 
    // immediately again (so that they don't get a already connected error) without waiting for 
    // the disconnect to actually happen as it is asynchronous.
    const clientToDeactivate = client;
    client = null;
    sessionId = null;
    clientToDeactivate.deactivate()
}


/**
 * The getSessionId function returns the users session Id for that connected session.
 * @author Alex Liu
 * @returns the users sessionId
 * @throws Error if attempting to get a null session id or if getting sessionId while already disconnected
 */
export function getSessionId(): string {
    if (client === null) {
        throw new Error("Attempted to get sessionId while already disconnected.");  
    }
    if (sessionId === null) {
        throw new Error("Attempted to get sessionId while it was null.");
    }
  
    return sessionId;

}




