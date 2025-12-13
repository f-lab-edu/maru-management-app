import { useQuery } from '@tanstack/react-query';
import { mockRangeAttendance } from '../mocks/attendanceMockData';
import type { RangeAttendanceResponse } from '../types';

const QUERY_KEYS = {
  range: (dojangId: number, startDate: string, endDate: string) =>
    ['attendance', 'range', dojangId, startDate, endDate] as const,
};

interface UseAttendanceRangeParams {
  dojangId: number | null;
  startDate: string;
  endDate: string;
}

/**
 * 기간별 출석 현황 조회
 *
 * @param params - 조회 파라미터
 * @param params.dojangId - 도장 ID
 * @param params.startDate - 시작일 (YYYY-MM-DD)
 * @param params.endDate - 종료일 (YYYY-MM-DD)
 * @returns 기간별 출석 현황 데이터
 */
export function useAttendanceRange({ dojangId, startDate, endDate }: UseAttendanceRangeParams) {
  return useQuery<RangeAttendanceResponse>({
    queryKey: QUERY_KEYS.range(dojangId ?? 0, startDate, endDate),
    queryFn: async () => {
      // TODO: API 연동
      // const response = await api.get('/api/v1/attendance/range', {
      //   params: { startDate, endDate }
      // });
      // return response.data.data;
      return mockRangeAttendance;
    },
    enabled: !!dojangId && !!startDate && !!endDate,
  });
}
