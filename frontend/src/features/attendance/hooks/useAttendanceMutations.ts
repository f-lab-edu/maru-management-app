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

/**
 * 출석 체크 mutation
 */
export function useAttendanceCheck(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<AttendanceResponse, Error, AttendanceCheckRequest>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.checkIn(dojangId, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
  });
}

/**
 * 일괄 출석 체크 mutation
 */
export function useBulkCheckIn(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<BulkCheckResponse, Error, BulkCheckRequest>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.bulkCheckIn(dojangId, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
  });
}

/**
 * 퇴관 처리 mutation
 */
export function useAttendanceCheckout(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<AttendanceResponse, Error, string>({
    mutationFn: (attendanceId) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.checkout(dojangId, attendanceId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
  });
}

/**
 * 퇴관 취소 mutation
 */
export function useCancelCheckout(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<AttendanceResponse, Error, string>({
    mutationFn: (attendanceId) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.cancelCheckout(dojangId, attendanceId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
  });
}

/**
 * 일괄 퇴관 처리 mutation
 */
export function useBulkCheckout(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<BulkCheckResponse, Error, string[]>({
    mutationFn: (attendanceIds) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.bulkCheckout(dojangId, attendanceIds);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
  });
}

/**
 * 출석 상태 수정 mutation
 */
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
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
  });
}

/**
 * 일괄 상태 변경 mutation
 */
export function useBulkStatusChange(dojangId: string | null) {
  const queryClient = useQueryClient();

  return useMutation<BulkCheckResponse, Error, BulkStatusChangeRequest>({
    mutationFn: (request) => {
      if (!dojangId) throw new Error('도장 ID가 필요합니다');
      return attendanceService.bulkChangeStatus(dojangId, request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
  });
}

/**
 * 시간 변경 mutation
 */
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
      queryClient.invalidateQueries({ queryKey: ['attendance'] });
    },
  });
}
