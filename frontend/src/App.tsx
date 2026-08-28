import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./components/Home";
import LobbyContextProvider from "./components/LobbyContextProvider";
import Lobby from "./components/Lobby";
/**
 * The frontend of the application.
 * @author Alex Liu
 */
function App() {
    return (
        <BrowserRouter>
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
