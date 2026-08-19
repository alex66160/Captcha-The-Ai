import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./components/Home";
import LobbyStateProvider from "./components/LobbyStateProvider";

/**
 * The frontend of the application.
 * @author Alex Liu
 * @returns the frontend
 */
function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Home />} />

                <Route
                    path="/:lobbyId"
                    element={<LobbyStateProvider></LobbyStateProvider>}
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
