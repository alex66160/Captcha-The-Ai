import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'


let client: Client | null = null;


export function connect(callback: () => void) {
    if (client !== null) {
        throw new Error("Attempted to connect while already connected.");  
    }
    client = new Client({
        brokerURL: "ws://localhost:8080/ws",
        onConnect: callback

    });

    client.activate();
}


export function subscribe(destination: string, callback: (message: IMessage) => void): StompSubscription {

    if (client === null) {
        throw new Error("Attempted to subscribe to destination: " + destination + " while disconnected.");
        
    }
    
   
    return client.subscribe(destination, callback);


}

export function publish<T>(destination: string, body: T) {

    if (client === null) {
        throw new Error("Attempted to publish to destination: " + destination + " while disconnected.");
    }
    return client.publish({destination: destination, body: JSON.stringify(body)});
}

export function disconnect() {
    if (client === null) {
        throw new Error("Attempted to disconnect while already disconnected.");  
    }
    client.deactivate().then(() => {client = null});
}




