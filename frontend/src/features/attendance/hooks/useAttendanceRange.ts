import { useQuery } from '@tanstack/react-query';
import { attendanceService } from '@/services/attendanceService';
import type { RangeAttendanceResponse } from '../types';

const QUERY_KEYS = {
  range: (dojangId: string, startDate: string, endDate: string) =>
    ['attendance', 'range', dojangId, startDate, endDate] as const,
};

interface UseAttendanceRangeParams {
  dojangId: string | null;
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
    queryKey: QUERY_KEYS.range(dojangId ?? '', startDate, endDate),
    queryFn: () => attendanceService.getRange(dojangId!, startDate, endDate),
    enabled: !!dojangId && !!startDate && !!endDate,
  });
}
