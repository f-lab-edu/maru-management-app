/**
 * 출석 관리 기능 타입 정의
 */

/**
 * 출석 상태
 */
export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'SICK' | 'EXCUSED';

/**
 * 뷰 모드 (주간/월간)
 */
export type ViewMode = 'weekly' | 'monthly';

/**
 * GET /today 응답 - 오늘 출석 현황
 */
export interface TodayAttendanceResponse {
  date: string;
  summary: {
    totalScheduled: number;
    present: number;
    absent: number;
    currentlyPresent: number;
  };
  students: TodayStudentRow[];
}

/**
 * 오늘 출석 현황 - 학생 행
 */
export interface TodayStudentRow {
  id: string;
  name: string;
  photoUrl: string | null;
  className: string;
  status: AttendanceStatus;
  checkinAt: string | null;
  checkoutAt: string | null;
}

/**
 * GET /range 응답 - 기간별 출석 현황
 */
export interface RangeAttendanceResponse {
  startDate: string;
  endDate: string;
  totalStudents: number;
  students: RangeStudentRow[];
}

/**
 * 기간별 출석 현황 - 학생 행
 */
export interface RangeStudentRow {
  id: string;
  name: string;
  photoUrl: string | null;
  className: string;
  attendances: Record<string, AttendanceCell | null>;
}

/**
 * 출석 셀 데이터 (날짜별 출석 정보)
 */
export interface AttendanceCell {
  status: AttendanceStatus;
  checkinAt: string | null;
}
