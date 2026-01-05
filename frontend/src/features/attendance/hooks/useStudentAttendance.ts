import { useQuery } from '@tanstack/react-query';
import { attendanceService } from '@/services/attendanceService';
import type { AttendanceResponse } from '../types';
import { attendanceKeys } from './useAttendanceMutations';

interface UseStudentAttendanceParams {
  dojangId: string | null;
  studentId: string | null;
  startDate: string;
  endDate: string;
}

export function useStudentAttendance({
  dojangId,
  studentId,
  startDate,
  endDate,
}: UseStudentAttendanceParams) {
  return useQuery<AttendanceResponse[]>({
    queryKey: attendanceKeys.student(dojangId ?? '', studentId ?? '', startDate, endDate),
    queryFn: () => attendanceService.getHistory(dojangId!, studentId!, startDate, endDate),
    enabled: !!dojangId && !!studentId && !!startDate && !!endDate,
    staleTime: 0,
    refetchOnMount: 'always',
  });
}
