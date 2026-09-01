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
            const size = Math.floor(Math.random() * 10) + 5;
            const fallDuration = size;
            const spawnLocation = Math.random() * 100;
            const id = crypto.randomUUID();
            setSquares((prev) => {
                // if (prev.length === 20) {
                //     return [
                //         ...prev.slice(1),
                //         { size, fallDuration, spawnLocation, id },
                //     ];
                // }
                return [...prev, { size, fallDuration, spawnLocation, id }];
            });
        }, 1000);

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
                            prev.filter((s) => s.id !== square.id),
                        );
                    }}
                ></div>
            ))}
        </div>
    );
}

export default Background;
