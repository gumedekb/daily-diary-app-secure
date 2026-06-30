import { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Login from './components/Login';
import Signup from "./components/Signup";
import Diary from "./components/Diary";
import { jwtDecode } from "jwt-decode";
import api, { clearToken } from "./api";


const App = () => {
  const [username, setUsername] = useState(null);

  // on app load, restore the session only if the token is present AND not expired
  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      try {
        const decoded = jwtDecode(token);
        if (decoded.exp && decoded.exp * 1000 > Date.now()) {
          setUsername(decoded.sub);
        } else {
          clearToken(); // expired token: drop it
        }
      } catch {
        clearToken(); // malformed token
      }
    }
  }, []);

  const handleLogin = (username) => setUsername(username);

  const handleLogout = async () => {
    try {
      // Tell the backend to revoke the token server-side, then remove it locally.
      await api.post('/auth/logout');
    } catch {
      // ignore network/credential errors on logout
    } finally {
      clearToken();
      setUsername(null);
    }
  };


  return (
    <Router>
      <Routes>
        <Route
          path="/"
          element={
            username ? (
              <Diary username={username} onLogout={handleLogout} />
            ) : (
              <Navigate to="/auth/login" />
            )
          }
        />
        <Route path="/auth/login" element={<Login onLogin={handleLogin} />} />
        <Route path="/auth/signup" element={<Signup />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;
