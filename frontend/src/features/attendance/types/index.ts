/**
 * 출석 관리 기능 타입 정의
 */

/**
 * 출석 상태
 */
export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'SICK' | 'EXCUSED';

/**
 * 체크 방법
 */
export type CheckMethod = 'MANUAL' | 'KIOSK' | 'ADMIN_APP' | 'AUTO';

/**
 * 뷰 모드 (주간/월간)
 */
export type ViewMode = 'weekly' | 'monthly';

/**
 * 출석 기록 응답 (AttendanceRes)
 */
export interface AttendanceResponse {
  id: number;
  studentId: number;
  studentName: string;
  status: AttendanceStatus;
  method: CheckMethod;
  attendanceDate: string;
  checkinAt: string | null;
  checkoutAt: string | null;
  note: string | null;
  createdAt: string;
}

/**
 * GET /today 응답 - 오늘 출석 현황 (CurrentAttendanceRes)
 */
export interface TodayAttendanceResponse {
  presentCount: number;
  expectedCount: number;
  currentlyPresent: number;
  attendanceList: AttendanceResponse[];
}

/**
 * 출석 정보 (AttendanceInfo) - 기간별 조회용
 */
export interface AttendanceInfo {
  id: number;
  status: AttendanceStatus;
  checkinAt: string | null;
  checkoutAt: string | null;
}

/**
 * 원생별 출석 행 (StudentAttendanceRowRes)
 */
export interface StudentAttendanceRow {
  id: number;
  name: string;
  photoUrl: string | null;
  className: string | null;
  attendances: Record<string, AttendanceInfo>;
}

/**
 * GET /range 응답 - 기간별 출석 현황 (RangeAttendanceRes)
 */
export interface RangeAttendanceResponse {
  startDate: string;
  endDate: string;
  totalStudents: number;
  students: StudentAttendanceRow[];
}

/**
 * 일괄 체크 실패 항목 (BulkCheckFailureRes)
 */
export interface BulkCheckFailure {
  studentId: number;
  errorMessage: string;
}

/**
 * 일괄 체크 응답 (BulkCheckRes)
 */
export interface BulkCheckResponse {
  successCount: number;
  failureCount: number;
  successList: AttendanceResponse[];
  failureList: BulkCheckFailure[];
}

/**
 * 출석 기록 생성 요청 (AttendanceCheckReq)
 */
export interface AttendanceCheckRequest {
  studentId: number;
  method: CheckMethod;
  status?: AttendanceStatus;
  date?: string;
  checkinAt?: string;
  note?: string;
}

/**
 * 일괄 출석 체크 요청 (BulkCheckReq)
 */
export interface BulkCheckRequest {
  studentIds: number[];
  method: CheckMethod;
}

/**
 * 출석 상태 변경 요청 (AttendanceStatusChangeReq)
 */
export interface AttendanceStatusChangeRequest {
  status: AttendanceStatus;
  note?: string;
}

/**
 * 일괄 상태 변경 요청 (BulkStatusChangeReq)
 */
export interface BulkStatusChangeRequest {
  attendanceIds: number[];
  status: AttendanceStatus;
  note?: string;
}

/**
 * 시간 변경 요청 (AttendanceTimeChangeReq)
 */
export interface AttendanceTimeChangeRequest {
  checkinAt?: string;
  checkoutAt?: string;
}
