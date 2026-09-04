import { useState, useEffect } from "react";

type Square = {
    size: number;
    fallDuration: number;
    spawnLocation: number;
    id: `${string}-${string}-${string}-${string}-${string}`;
};

function Background() {
    const [squares, setSquares] = useState<Square[]>([]);
    useEffect(() => {
        const spawnSquareInterval = setInterval(() => {
            const size = Math.random() * 10 + 2;
            console.log("SIZE: " + size);
            const fallDuration = 50 / size;
            console.log("FALL DURATION: " + fallDuration);
            const spawnLocation = Math.random() * 100;
            // use id so we can remove it properly and so the .map can track squares properly
            const id = crypto.randomUUID();
            setSquares((prev) => [
                ...prev,
                { size, fallDuration, spawnLocation, id },
            ]);

            // Control how fast the squares spawn, right now it spawn a square every .5 seconds
        }, 500);

        return () => {
            clearInterval(spawnSquareInterval);
        };
    }, []);
    return (
        <div className="fixed inset-0 bg-blue-500 -z-10">
            {squares.map((square) => (
                <div
                    key={square.id}
                    className="absolute square-animation aspect-square bg-white"
                    style={{
                        width: `${square.size}%`,

                        left: `${square.spawnLocation}%`,
                        animationDuration: `${square.fallDuration}s`,
                    }}
                    onAnimationEnd={() => {
                        setSquares((prev) =>
                            prev.filter(
                                (squareFromArray) =>
                                    squareFromArray.id !== square.id,
                            ),
                        );
                    }}
                ></div>
            ))}
        </div>
    );
}

export default Background;
