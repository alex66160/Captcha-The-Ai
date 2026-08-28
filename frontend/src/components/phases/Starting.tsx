import { useState, useEffect } from "react";
const tutorialSlides = [
    "Welcome To Captcha the Ai! Every round, a Question Writer will be chosen to write a question.",
    "After the Question is written, other players will submit an Answer, and then voting will begin.",
    "Work with other players to figure out who the Ai player is, and vote them out to win! Good luck!",
];

/**
 * The Starting component displays the tutorial slides for the game.
 * @author Alex Liu
 */
function Starting() {
    const [currentSlideIndex, setCurrentSlideIndex] = useState(0);

    useEffect(() => {
        // Use setInterval to go to the next slide every 5 seconds.
        const nextSlideInterval = setInterval(() => {
            setCurrentSlideIndex((prev) => (prev + 1) % tutorialSlides.length);
        }, 5000);

        return () => {
            clearInterval(nextSlideInterval);
        };
        // We store the currentSlideIndex in the dependency array so that when the user changes the slide
        // it'll automatically tear down the interval and create a new one. This will guarantee it'll be 5 seconds
        // before the next slide changes again.
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
