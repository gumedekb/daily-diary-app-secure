import { useState } from "react"
import { useNavigate } from "react-router-dom";
import api from "../api";

const Login = ({ onLogin }) => {

   const [username, setUsername] = useState('');
   const [password, setPassword] = useState('');
   const [error, setError] = useState(null);
   const navigate = useNavigate();

   const handleSubmit = async (e) => {
      e.preventDefault();
      setError(null);

      try {
         const response = await api.post('/auth/login', { username, password });
         localStorage.setItem('jwtToken', response.data.token);
         onLogin(response.data.username);
         navigate("/diary"); // 👈 redirect to diary page
      } catch (err) {
         // Surface backend messages (e.g. rate-limit lockout). Body may be a
         // plain string or a { message } object.
         const data = err.response?.data;
         const message = typeof data === "string" ? data : data?.message;
         setError(message || 'Invalid username or password');
      }
   };

   return (
      <div className="min-h-screen flex items-center justify-center bg-gray-900 text-gray-100 p-4">
         <form onSubmit={handleSubmit} className="w-full max-w-md bg-gray-800 shadow-md rounded-lg p-8 space-y-6">
            <h2 className="text-2xl font-bold text-center">Login</h2>
            {error && <div className="text-red-400 text-center">{error}</div>}

            <input 
               type="text" 
               placeholder="Username"
               value={username}
               onChange={(e) => setUsername(e.target.value)}
               className="w-full px-4 py-2 bg-gray-700 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
               required
            />

            <input 
               type="password"
               placeholder="Password"
               value={password}
               onChange={(e) => setPassword(e.target.value)}
               className="w-full px-4 py-2 bg-gray-700 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
               required
            />

            <button
               type="submit"
               className="w-full py-2 bg-indigo-600 rounded hover:bg-indigo-500 transition"
            >
               Login
            </button>

            <p className="text-sm text-center">
            Don't have an account?{" "}
            <button
               type="button"
               onClick={() => window.location.href = "/auth/signup"}
               className="text-indigo-400 hover:underline"
            >
               Sign up
            </button>
            </p>
         </form>
      </div>
   );
}

export default Login;