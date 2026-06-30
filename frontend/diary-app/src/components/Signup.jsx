import { useState } from "react";
import api from "../api";
import { useNavigate } from "react-router-dom";

export default function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
  });
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      await api.post("/auth/signup", form);
      setSuccess("Account created successfully!");
      setTimeout(() => navigate("/auth/login"), 1500); // redirect to login
    } catch (err) {
      // Backend may return a plain string body or a { message } JSON object.
      const data = err.response?.data;
      const message =
        typeof data === "string" ? data : data?.message;
      setError(message || "Signup failed");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-900 text-gray-100 p-4">
      <form onSubmit={handleSubmit} className="w-full max-w-md bg-gray-800 shadow-md rounded-lg p-8 space-y-6">
        <h2 className="text-2xl font-bold text-center">Sign Up</h2>
        {error && <div className="text-red-400 text-center">{error}</div>}
        {success && <div className="text-green-400 text-center">{success}</div>}

        <input
          type="text"
          name="username"
          placeholder="Username"
          value={form.username}
          onChange={handleChange}
          className="w-full px-4 py-2 bg-gray-700 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
          required
        />
        <input
          type="email"
          name="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          className="w-full px-4 py-2 bg-gray-700 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
          required
        />
        <input
          type="password"
          name="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange}
          className="w-full px-4 py-2 bg-gray-700 rounded focus:outline-none focus:ring-2 focus:ring-indigo-500"
          required
        />
        <button type="submit" className="w-full py-2 bg-indigo-600 rounded hover:bg-indigo-500 transition">
          Create Account
        </button>

        <p className="text-sm text-center">
          Already have an account?{" "}
          <button
            type="button"
            onClick={() => navigate("/auth/login")}
            className="text-indigo-400 hover:underline"
          >
            Log in
          </button>
        </p>
      </form>
    </div>
  );
}
