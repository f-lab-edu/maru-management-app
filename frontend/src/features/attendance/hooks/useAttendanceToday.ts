import { useQuery } from '@tanstack/react-query';
import { mockTodayAttendance } from '../mocks/attendanceMockData';
import type { TodayAttendanceResponse } from '../types';

const QUERY_KEYS = {
  today: (dojangId: number) => ['attendance', 'today', dojangId] as const,
};

/**
 * 오늘 출석 현황 조회
 *
 * @param dojangId - 도장 ID
 * @returns 오늘 출석 현황 데이터
 */
export function useAttendanceToday(dojangId: number | null) {
  return useQuery<TodayAttendanceResponse>({
    queryKey: QUERY_KEYS.today(dojangId ?? 0),
    queryFn: async () => {
      // TODO: API 연동
      // const response = await api.get('/api/v1/attendance/today');
      // return response.data.data;
      return mockTodayAttendance;
    },
    enabled: !!dojangId,
    staleTime: 30 * 1000, // 30초 (실시간성 필요)
  });
}
