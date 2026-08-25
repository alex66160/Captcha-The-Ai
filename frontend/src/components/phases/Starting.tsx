import { useState, useEffect } from "react";
const tutorialSlides = [
    "Welcome To Captcha the Ai! Every round, a Question Writer will be chosen to write a question.",
    "After the Question is written, other players will submit an Answer, and then voting will begin.",
    "Work with other players to figure out who the Ai player is, and vote them out to win! Good luck!",
];

function Starting() {
    const [currentSlideIndex, setCurrentSlideIndex] = useState(0);

    useEffect(() => {
        const nextSlideInterval = setInterval(() => {
            setCurrentSlideIndex((prev) => (prev + 1) % tutorialSlides.length);
        }, 5000);

        return () => {
            clearInterval(nextSlideInterval);
        };
    }, [currentSlideIndex]);

    return (
        <div>
            <button
                onClick={() =>
                    setCurrentSlideIndex(
                        (prev) =>
                            (prev - 1 + tutorialSlides.length) %
                            tutorialSlides.length,
                    )
                }
            >
                Show prev slide
            </button>
            <p>{tutorialSlides[currentSlideIndex]}</p>
            <button
                onClick={() =>
                    setCurrentSlideIndex(
                        (prev) => (prev + 1) % tutorialSlides.length,
                    )
                }
            >
                show next slide
            </button>
        </div>
    );
}

export default Starting;
