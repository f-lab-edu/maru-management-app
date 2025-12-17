import { useQuery } from '@tanstack/react-query';
import { attendanceService } from '@/services/attendanceService';
import type { AttendanceResponse } from '../types';

const QUERY_KEYS = {
  studentDetail: (dojangId: number, studentId: number, startDate: string, endDate: string) =>
    ['attendance', 'student', dojangId, studentId, startDate, endDate] as const,
};

interface UseStudentAttendanceParams {
  dojangId: number | null;
  studentId: number | null;
  startDate: string;
  endDate: string;
}

/**
 * 특정 학생의 출석 상세 정보 조회
 */
export function useStudentAttendance({
  dojangId,
  studentId,
  startDate,
  endDate,
}: UseStudentAttendanceParams) {
  return useQuery<AttendanceResponse[]>({
    queryKey: QUERY_KEYS.studentDetail(dojangId ?? 0, studentId ?? 0, startDate, endDate),
    queryFn: () => attendanceService.getHistory(dojangId!, studentId!, startDate, endDate),
    enabled: !!dojangId && !!studentId && !!startDate && !!endDate,
    staleTime: 0,
    refetchOnMount: 'always',
  });
}
