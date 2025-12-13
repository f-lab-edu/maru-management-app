import { useQuery } from '@tanstack/react-query';
import { mockRangeAttendance } from '../mocks/attendanceMockData';
import type { RangeStudentRow } from '../types';

const QUERY_KEYS = {
  studentDetail: (dojangId: number, studentId: string) =>
    ['attendance', 'student', dojangId, studentId] as const,
};

interface UseStudentAttendanceParams {
  dojangId: number | null;
  studentId: string | null;
}

interface StudentAttendanceDetail {
  student: RangeStudentRow | null;
  startDate: string;
  endDate: string;
}

/**
 * 특정 학생의 출석 상세 정보 조회
 * 최근 30일간의 출석 이력을 조회합니다.
 */
export function useStudentAttendance({ dojangId, studentId }: UseStudentAttendanceParams) {
  return useQuery<StudentAttendanceDetail>({
    queryKey: QUERY_KEYS.studentDetail(dojangId ?? 0, studentId ?? ''),
    queryFn: async () => {
      // TODO: API 연동
      // const response = await api.get(`/api/v1/attendance/students/${studentId}`, {
      //   params: { days: 30 }
      // });
      // return response.data.data;

      // Mock: 기존 range 데이터에서 학생 필터링
      const student = mockRangeAttendance.students.find((s) => s.id === studentId) ?? null;
      return {
        student,
        startDate: mockRangeAttendance.startDate,
        endDate: mockRangeAttendance.endDate,
      };
    },
    enabled: !!dojangId && !!studentId,
  });
}
