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

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

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
    const status = error.response?.status;
    const errorCode = error.response?.data?.code;

    // 401 Unauthorized - 인증 만료/실패
    if (status === 401) {
      Swal.fire({
        icon: 'warning',
        title: '로그인이 필요합니다',
        text: '세션이 만료되었습니다. 다시 로그인해주세요.',
        confirmButtonText: '로그인',
        allowOutsideClick: false,
      }).then((result) => {
        if (result.isConfirmed) {
          window.location.href = '/login';
        }
      });

      return new Promise(() => {});
    }

    // 403 Forbidden - 권한 없음 (인증 관련 에러는 제외)
    if (status === 403) {
      // 에러 코드가 없거나 AUTH_로 시작하면 인증 문제 → 로그인으로
      const isAuthError = !errorCode || errorCode.startsWith('AUTH_');

      if (isAuthError) {
        Swal.fire({
          icon: 'warning',
          title: '로그인이 필요합니다',
          text: '세션이 만료되었습니다. 다시 로그인해주세요.',
          confirmButtonText: '로그인',
          allowOutsideClick: false,
        }).then((result) => {
          if (result.isConfirmed) {
            window.location.href = '/login';
          }
        });
      } else {
        // 권한 관련 에러 (PERMISSION_, ACCESS_ 등)
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
      }

      return new Promise(() => {});
    }

    return Promise.reject(error);
  }
);

export default apiClient;
