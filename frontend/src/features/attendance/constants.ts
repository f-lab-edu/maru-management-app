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
 * 출석 상태별 색상 (Tailwind CSS 클래스) - 모노크롬
 */
export const ATTENDANCE_STATUS_COLORS: Record<AttendanceStatus, string> = {
  PRESENT: 'bg-foreground text-background',
  ABSENT: 'bg-muted-foreground/70 text-background',
  SICK: 'bg-muted-foreground/50 text-background',
  EXCUSED: 'bg-muted-foreground/30 text-foreground',
};

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
