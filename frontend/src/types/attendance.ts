/**
 * 출석 관리 API 응답 래퍼 타입 정의
 */

import type {
  TodayAttendanceResponse,
  RangeAttendanceResponse,
} from '@/features/attendance/types';

/**
 * API 응답 공통 래퍼
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

/**
 * 오늘 출석 현황 API 응답
 */
export type TodayAttendanceApiResponse = ApiResponse<TodayAttendanceResponse>;

/**
 * 기간별 출석 현황 API 응답
 */
export type RangeAttendanceApiResponse = ApiResponse<RangeAttendanceResponse>;
