import { useMutation, useQueryClient } from '@tanstack/react-query';
import { attendanceService } from '@/services/attendanceService';
import type {
  AttendanceResponse,
  BulkCheckResponse,
  AttendanceCheckRequest,
  BulkCheckRequest,
  AttendanceStatusChangeRequest,
  BulkStatusChangeRequest,
  AttendanceTimeChangeRequest,
} from '../types';

export const attendanceKeys = {
  all: (dojangId: string) => ['attendance', dojangId] as const,
  range: (dojangId: string, startDate: string, endDate: string, filters?: object) =>
    [...attendanceKeys.all(dojangId), 'range', startDate, endDate, filters] as const,
  student: (dojangId: string, studentId: string, startDate: string, endDate: string) =>
    [...attendanceKeys.all(dojangId), 'student', studentId, startDate, endDate] as const,
};

export function useAttendanceCheck(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<AttendanceResponse, Error, AttendanceCheckRequest>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.checkIn(dojangId, request);
    },
    onSuccess: () => {
      if (dojangId) {
        queryClient.invalidateQueries({ queryKey: attendanceKeys.all(dojangId) });
      }
    },
  });
}

export function useBulkCheckIn(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<BulkCheckResponse, Error, BulkCheckRequest>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.bulkCheckIn(dojangId, request);
    },
    onSuccess: () => {
      if (dojangId) {
        queryClient.invalidateQueries({ queryKey: attendanceKeys.all(dojangId) });
      }
    },
  });
}

export function useAttendanceCheckout(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<AttendanceResponse, Error, string>({
    mutationFn: (attendanceId) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.checkout(dojangId, attendanceId);
    },
    onSuccess: () => {
      if (dojangId) {
        queryClient.invalidateQueries({ queryKey: attendanceKeys.all(dojangId) });
      }
    },
  });
}

export function useCancelCheckout(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<AttendanceResponse, Error, string>({
    mutationFn: (attendanceId) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.cancelCheckout(dojangId, attendanceId);
    },
    onSuccess: () => {
      if (dojangId) {
        queryClient.invalidateQueries({ queryKey: attendanceKeys.all(dojangId) });
      }
    },
  });
}

export function useBulkCheckout(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<BulkCheckResponse, Error, string[]>({
    mutationFn: (attendanceIds) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.bulkCheckout(dojangId, attendanceIds);
    },
    onSuccess: () => {
      if (dojangId) {
        queryClient.invalidateQueries({ queryKey: attendanceKeys.all(dojangId) });
      }
    },
  });
}

export function useAttendanceStatusUpdate(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<
    AttendanceResponse,
    Error,
    { attendanceId: string; request: AttendanceStatusChangeRequest }
  >({
    mutationFn: ({ attendanceId, request }) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.changeStatus(dojangId, attendanceId, request);
    },
    onSuccess: () => {
      if (dojangId) {
        queryClient.invalidateQueries({ queryKey: attendanceKeys.all(dojangId) });
      }
    },
  });
}

export function useBulkStatusChange(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<BulkCheckResponse, Error, BulkStatusChangeRequest>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.bulkChangeStatus(dojangId, request);
    },
    onSuccess: () => {
      if (dojangId) {
        queryClient.invalidateQueries({ queryKey: attendanceKeys.all(dojangId) });
      }
    },
  });
}

export function useAttendanceTimeChange(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<
    AttendanceResponse,
    Error,
    { attendanceId: string; request: AttendanceTimeChangeRequest }
  >({
    mutationFn: ({ attendanceId, request }) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.changeTime(dojangId, attendanceId, request);
    },
    onSuccess: () => {
      if (dojangId) {
        queryClient.invalidateQueries({ queryKey: attendanceKeys.all(dojangId) });
      }
    },
  });
}
