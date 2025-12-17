import apiClient from './api';
import type {
  RangeAttendanceResponse,
  AttendanceResponse,
  BulkCheckResponse,
  AttendanceCheckRequest,
  BulkCheckRequest,
  AttendanceStatusChangeRequest,
  BulkStatusChangeRequest,
  AttendanceTimeChangeRequest,
} from '@/features/attendance/types';

const BASE_PATH = '/attendance';

export const attendanceService = {
  getRange: async (
    dojangId: number,
    startDate: string,
    endDate: string
  ): Promise<RangeAttendanceResponse> => {
    const response = await apiClient.get<RangeAttendanceResponse>(`${BASE_PATH}/range`, {
      params: { dojangId, startDate, endDate },
    });
    return response.data;
  },

  checkIn: async (
    dojangId: number,
    request: AttendanceCheckRequest
  ): Promise<AttendanceResponse> => {
    const response = await apiClient.post<AttendanceResponse>(`${BASE_PATH}/check`, request, {
      params: { dojangId },
    });
    return response.data;
  },

  bulkCheckIn: async (dojangId: number, request: BulkCheckRequest): Promise<BulkCheckResponse> => {
    const response = await apiClient.post<BulkCheckResponse>(`${BASE_PATH}/bulk-check`, request, {
      params: { dojangId },
    });
    return response.data;
  },

  checkout: async (dojangId: number, attendanceId: number): Promise<AttendanceResponse> => {
    const response = await apiClient.patch<AttendanceResponse>(
      `${BASE_PATH}/${attendanceId}/checkout`,
      null,
      { params: { dojangId } }
    );
    return response.data;
  },

  cancelCheckout: async (dojangId: number, attendanceId: number): Promise<AttendanceResponse> => {
    const response = await apiClient.patch<AttendanceResponse>(
      `${BASE_PATH}/${attendanceId}/cancel-checkout`,
      null,
      { params: { dojangId } }
    );
    return response.data;
  },

  bulkCheckout: async (dojangId: number, attendanceIds: number[]): Promise<BulkCheckResponse> => {
    const response = await apiClient.post<BulkCheckResponse>(
      `${BASE_PATH}/bulk-checkout`,
      { attendanceIds },
      { params: { dojangId } }
    );
    return response.data;
  },

  changeStatus: async (
    dojangId: number,
    attendanceId: number,
    request: AttendanceStatusChangeRequest
  ): Promise<AttendanceResponse> => {
    const response = await apiClient.patch<AttendanceResponse>(
      `${BASE_PATH}/${attendanceId}/status`,
      request,
      { params: { dojangId } }
    );
    return response.data;
  },

  changeTime: async (
    dojangId: number,
    attendanceId: number,
    request: AttendanceTimeChangeRequest
  ): Promise<AttendanceResponse> => {
    const response = await apiClient.patch<AttendanceResponse>(
      `${BASE_PATH}/${attendanceId}/time`,
      request,
      { params: { dojangId } }
    );
    return response.data;
  },

  bulkChangeStatus: async (
    dojangId: number,
    request: BulkStatusChangeRequest
  ): Promise<BulkCheckResponse> => {
    const response = await apiClient.post<BulkCheckResponse>(
      `${BASE_PATH}/bulk-status`,
      request,
      { params: { dojangId } }
    );
    return response.data;
  },

  getHistory: async (
    dojangId: number,
    studentId: number,
    startDate: string,
    endDate: string
  ): Promise<AttendanceResponse[]> => {
    const response = await apiClient.get<AttendanceResponse[]>(`${BASE_PATH}/history`, {
      params: { dojangId, studentId, startDate, endDate },
    });
    return response.data;
  },
};
