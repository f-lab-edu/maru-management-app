import axios from 'axios';
import Swal from 'sweetalert2';

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
  (error) => {
    // 403 Forbidden - 권한 변경됨
    if (error.response?.status === 403) {
      Swal.fire({
        icon: 'warning',
        title: '권한이 변경되었습니다',
        text: '확인을 누르면 대시보드로 이동합니다.',
        confirmButtonText: '확인',
        allowOutsideClick: false,
      }).then((result) => {
        if (result.isConfirmed) {
          window.location.href = '/dashboard';
        }
      });

      // 에러 전파 중단 (UI에서 추가 에러 처리 방지)
      return new Promise(() => {});
    }

    return Promise.reject(error);
  }
);

export default apiClient;
