import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./components/Home";
import LobbyContextProvider from "./components/LobbyContextProvider";
import Lobby from "./components/Lobby";
import Background from "./components/Background";
/**
 * The frontend of the application.
 * @author Alex Liu
 */
function App() {
    return (
        <BrowserRouter>
            <Background />
            <Routes>
                <Route path="/" element={<Home />} />

                <Route
                    path="/:lobbyId"
                    element={
                        <LobbyContextProvider>
                            <Lobby />
                        </LobbyContextProvider>
                    }
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
