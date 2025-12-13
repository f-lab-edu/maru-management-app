import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { AttendanceStatus } from '../types';

/**
 * 출석 체크 요청 타입
 */
export interface AttendanceCheckRequest {
  studentId: string;
  date: string;
  status: AttendanceStatus;
  checkinAt: string;
  memo?: string;
}

/**
 * 출석 체크 응답 타입
 */
export interface AttendanceCheckResponse {
  id: string;
  studentId: string;
  status: AttendanceStatus;
  date: string;
  checkinAt: string;
  memo?: string;
}

/**
 * 출석 상태 수정 요청 타입
 */
interface AttendanceStatusUpdateRequest {
  id: string;
  status: AttendanceStatus;
  notes?: string;
}

/**
 * 퇴관 처리 요청 타입
 */
interface AttendanceCheckoutRequest {
  id: string;
  checkoutAt: string;
}

/**
 * 출석 체크 mutation
 */
export function useAttendanceCheck(_dojangId: number | null) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: AttendanceCheckRequest): Promise<AttendanceCheckResponse> => {
      console.log('출석 체크:', data);

      await new Promise((resolve) => setTimeout(resolve, 500));

      return {
        id: `attendance-${Date.now()}`,
        ...data,
      };
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
  });
}

/**
 * 출석 상태 수정 mutation
 */
export function useAttendanceStatusUpdate(_dojangId: number | null) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: AttendanceStatusUpdateRequest) => {
      console.log('상태 수정:', data);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
    onError: (error) => {
      console.error('상태 수정 실패:', error);
      throw new Error('출석 상태 수정에 실패했습니다');
    },
  });
}

/**
 * 퇴관 처리 mutation
 */
export function useAttendanceCheckout(_dojangId: number | null) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: AttendanceCheckoutRequest) => {
      console.log('퇴관 처리:', data);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
    onError: (error) => {
      console.error('퇴관 처리 실패:', error);
      throw new Error('퇴관 처리에 실패했습니다');
    },
  });
}
