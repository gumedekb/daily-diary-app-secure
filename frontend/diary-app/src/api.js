import axios from "axios";

const TOKEN_KEY = "jwtToken";

const api = axios.create({
   baseURL: 'https://daily-diary-app-secure.onrender.com/api',
});

// Helper so logout / expiry handling has a single source of truth.
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

// Intercept requests to add Authorization header if token exists
api.interceptors.request.use(config => {
   const token = localStorage.getItem(TOKEN_KEY);
   if (token) {
      config.headers.Authorization = `Bearer ${token}`;
   }
   return config;
});

// If the server rejects our token (expired/revoked), drop it and force re-login.
api.interceptors.response.use(
   response => response,
   error => {
      const status = error?.response?.status;
      const url = error?.config?.url || "";
      // Don't bounce on a failed login/signup attempt — only on authenticated calls.
      const isAuthAttempt = url.includes("/auth/login") || url.includes("/auth/signup");
      if (status === 401 && !isAuthAttempt) {
         clearToken();
         if (window.location.pathname !== "/auth/login") {
            window.location.href = "/auth/login";
         }
      }
      return Promise.reject(error);
   }
);

export default api;
