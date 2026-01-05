import axios from 'axios';

export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path?: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const baseURL = `${API_BASE_URL}/api/v1`;

const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
  withCredentials: true,
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(error)
);

export default apiClient;
