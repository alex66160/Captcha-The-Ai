import { useEffect, useState } from "react";
import { subscribe } from "./StompActions.ts";

type StatsBroadcast = { totalPlayerCount: number; totalLobbyCount: number };

/**
 * The Stats component recieves the stats broadcast from the backend, which includes
 * the total player and lobby count and displays it.
 * @author Alex Liu
 */
function Stats() {
    const [totalPlayerCount, setTotalPlayerCount] = useState(0);
    const [totalLobbyCount, setTotalLobbyCount] = useState(0);

    useEffect(() => {
        const statsSubscription = subscribe("/topic/stats", (message) => {
            const statsBroadcast: StatsBroadcast = JSON.parse(message.body);
            setTotalPlayerCount(statsBroadcast.totalPlayerCount);
            setTotalLobbyCount(statsBroadcast.totalLobbyCount);
        });

        return () => {
            statsSubscription.unsubscribe();
        };
    }, []);

    return (
        <p>
            TOTAL PLAYER COUNT: {totalPlayerCount} TOTAL LOBBY COUNT:{" "}
            {totalLobbyCount}
        </p>
    );
}

export default Stats;
