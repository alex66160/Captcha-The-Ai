import { BrowserRouter, Routes, Route } from "react-router-dom";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Home />} />

                <Route path="/:lobbyId" element={} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
