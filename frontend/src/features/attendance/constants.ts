/**
 * 출석 관리 상수 정의
 */

import type { AttendanceStatus, ViewMode } from './types';

/**
 * 출석 상태별 한글 라벨
 */
export const ATTENDANCE_STATUS_LABELS: Record<AttendanceStatus, string> = {
  PRESENT: '출석',
  ABSENT: '결석',
  SICK: '병결',
  EXCUSED: '공결',
};

/**
 * 출석 + 퇴관 여부에 따른 라벨
 * - 수업중: 출석했지만 아직 퇴관 안 함
 * - 출석: 출석하고 퇴관 완료
 */
export const ATTENDANCE_DISPLAY_LABELS = {
  IN_CLASS: '수업중',
  CHECKED_OUT: '출석',
} as const;

/**
 * 출석 상태별 색상 (Tailwind CSS 클래스)
 */
export const ATTENDANCE_STATUS_COLORS: Record<AttendanceStatus, string> = {
  PRESENT: 'bg-emerald-600 text-white',
  ABSENT: 'bg-red-500 text-white',
  SICK: 'bg-amber-500 text-white',
  EXCUSED: 'bg-blue-500 text-white',
};

/**
 * 수업중 (출석했지만 퇴관 안 함) 색상 - 진한 초록
 */
export const IN_CLASS_COLOR = 'bg-emerald-600 text-white';

/**
 * 출석 완료 (퇴관 완료) 색상 - 연한 초록
 */
export const CHECKED_OUT_COLOR = 'bg-emerald-400 text-white';

/**
 * 뷰 모드 옵션
 */
export const VIEW_MODE_OPTIONS: { value: ViewMode; label: string }[] = [
  { value: 'weekly', label: '주간' },
  { value: 'monthly', label: '월간' },
];

/**
 * 요일 한글 라벨
 */
export const WEEKDAY_LABELS = ['일', '월', '화', '수', '목', '금', '토'];
