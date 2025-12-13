import { useMemo } from 'react';
import type { RangeStudentRow, AttendanceStatus } from '../types';

interface UseAttendanceFilterParams {
  students: RangeStudentRow[];
  searchQuery: string;
  statusFilter: AttendanceStatus | 'ALL';
}

/**
 * 출석 현황 필터링
 *
 * @param params - 필터링 파라미터
 * @param params.students - 원생 목록
 * @param params.searchQuery - 이름 검색어
 * @param params.statusFilter - 상태 필터 (ALL | PRESENT | ABSENT | SICK | EXCUSED)
 * @returns 필터링된 원생 목록
 */
export function useAttendanceFilter({ students, searchQuery, statusFilter }: UseAttendanceFilterParams) {
  return useMemo(() => {
    let filtered = students;

    // 이름 검색
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter((student) => student.name.toLowerCase().includes(query));
    }

    // 상태 필터 (특정 날짜에 해당 상태가 있는 원생만)
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter((student) => {
        // 모든 출석 기록 중 하나라도 해당 상태가 있는지 확인
        return Object.values(student.attendances).some(
          (attendance) => attendance !== null && attendance.status === statusFilter
        );
      });
    }

    return filtered;
  }, [students, searchQuery, statusFilter]);
}
