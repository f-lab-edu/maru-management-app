import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { attendanceService, type AttendanceFilterParams } from '@/services/attendanceService';
import type { RangeAttendanceResponse } from '../types';
import { attendanceKeys } from './useAttendanceMutations';

interface UseAttendanceRangeParams {
  dojangId: string | null;
  startDate: string;
  endDate: string;
  sectionId?: string | null;
  divisionId?: string | null;
}

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
    queryKey: attendanceKeys.range(dojangId ?? '', startDate, endDate, filters),
    queryFn: () => attendanceService.getRange(dojangId!, startDate, endDate, filters),
    enabled: !!dojangId && !!startDate && !!endDate,
    placeholderData: keepPreviousData,
  });
}
