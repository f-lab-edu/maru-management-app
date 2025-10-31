import axios from "axios";

// Prefer Vite env var; fallback to "/api" to use Vite/Nginx proxy and avoid CORS
const baseURL = (import.meta as any)?.env?.VITE_API_URL ?? "/api";

export const api = axios.create({
  baseURL,
  headers: {
    Accept: "application/json, text/plain, */*",
  },
});
