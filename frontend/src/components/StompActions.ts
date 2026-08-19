import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'

/** The stomp client that holds the actual connection and provides stomp actions. */
let client: Client | null = null;

/** The users sessionId for the current connected session. */
let sessionId: string | null = null;


/**
 * The connect function connects to the backend, and then runs a given callback.
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
        onConnect: (frame) => {
            // Store the sessionId once it connects, we do this so that user /queue can be setup properly.
            sessionId = frame.headers.session;
            callback();
        }
    });

    client.activate();
}

/**
 * The subscribe function allows the user to subscribe to a destination and provide a callback to run when messages are
 * published to that destination. The user is responsible for storing the StompSubscription when calling this function, so
 * that they can unsubscribe.
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
 * The subscribeAndWait function allows the user to subscribe to a destination and provide a callback to run when messages are
 * published to that destination, and the user is responsible for storing the StompSubscription if its successful so that they can manage
 * the unsubscribe. In addition, it makes sure that the subscribe finished before returning, unlike the other subscribe function.
 * @param destination the destination to subscribe to
 * @param callback  the behavior to run whenever something is published to that destination
 * @returns the promise which contains the subscription that was created
 * @throws Error if attempting to subscribe without a connection
 */
export function subscribeAndWait(destination: string, callback: (message: IMessage) => void): Promise<StompSubscription>  {

   return new Promise((resolve) => {
        if (client === null) {
            throw new Error("Attempted to subscribe to destination: " + destination + " while disconnected.");
        }
        // The receipt to attatch to the subscribe so we have a way of knowing when its finished
        const receipt = crypto.randomUUID();

        // Monitor receipt before subscribing so that we don't accidently have a race condition where
        // it subscribes before receipt monitor starts
        client.watchForReceipt(receipt, () => {
            resolve(subscription)
        });
        const subscription = client.subscribe(destination, callback, {receipt: receipt});
    }

    );
}


/**
 * The publish function allows the user to publish data to a destination.
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
    client.publish({destination: destination, body: JSON.stringify(data)});
}

/**
 * The disconnect function allows the user to disconnect from the backend.
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


