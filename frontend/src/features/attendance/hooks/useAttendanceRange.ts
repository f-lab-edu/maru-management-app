import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { attendanceService, type AttendanceFilterParams } from '@/services/attendanceService';
import type { RangeAttendanceResponse } from '../types';

const QUERY_KEYS = {
  range: (dojangId: string, startDate: string, endDate: string, filters?: AttendanceFilterParams) =>
    ['attendance', 'range', dojangId, startDate, endDate, filters] as const,
};

interface UseAttendanceRangeParams {
  dojangId: string | null;
  startDate: string;
  endDate: string;
  sectionId?: string | null;
  divisionId?: string | null;
}

/**
 * 기간별 출석 현황 조회
 *
 * @param params - 조회 파라미터
 * @param params.dojangId - 도장 ID
 * @param params.startDate - 시작일 (YYYY-MM-DD)
 * @param params.endDate - 종료일 (YYYY-MM-DD)
 * @param params.sectionId - 수련부 ID (선택)
 * @param params.divisionId - 수련반 ID (선택)
 * @returns 기간별 출석 현황 데이터
 */
export function useAttendanceRange({
  dojangId,
  startDate,
  endDate,
  sectionId,
  divisionId,
}: UseAttendanceRangeParams) {
  const filters: AttendanceFilterParams = {
    sectionId: sectionId ?? undefined,
    divisionId: divisionId ?? undefined,
  };

  return useQuery<RangeAttendanceResponse>({
    queryKey: QUERY_KEYS.range(dojangId ?? '', startDate, endDate, filters),
    queryFn: () => attendanceService.getRange(dojangId!, startDate, endDate, filters),
    enabled: !!dojangId && !!startDate && !!endDate,
    placeholderData: keepPreviousData,
  });
}
