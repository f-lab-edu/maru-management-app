import axios from 'axios';

export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path?: string;
}

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
  withCredentials: true,
});

// TODO : 클라이언트 에러 처리 인터렉션
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      const status = error.response.status;

      if (status === 401) {
        window.location.href = '/login';
      } else if (status === 403) {
        console.warn('권한이 없습니다:', error.response.data);
      } else if (status === 500) {
        console.error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
