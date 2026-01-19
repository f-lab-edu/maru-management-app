import apiClient from './api';
import {
  DojangSearchResult,
  Employment,
  Instructor,
  InstructorDetail,
  PendingApprovalRequest,
} from '../types/employment';

export interface PagedResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const employmentService = {
  searchDojangs: async (
    keyword: string,
    page: number = 0,
    size: number = 5,
  ): Promise<PagedResult<DojangSearchResult>> => {
    const response = await apiClient.get<PagedResult<DojangSearchResult>>('/dojangs/search', {
      params: { keyword, page, size },
    });
    return response.data;
  },

  requestApproval: async (dojangId: string): Promise<Employment> => {
    const response = await apiClient.post<Employment>('/employments/request', null, {
      params: { dojangId },
    });
    return response.data;
  },

  getMyRequests: async (): Promise<Employment[]> => {
    const response = await apiClient.get<Employment[]>('/employments/my-requests');
    return response.data;
  },

  getPendingRequests: async (): Promise<PendingApprovalRequest[]> => {
    const response = await apiClient.get<PendingApprovalRequest[]>('/employments/pending');
    return response.data;
  },

  approve: async (employmentId: string): Promise<Employment> => {
    const response = await apiClient.patch<Employment>(`/employments/${employmentId}/approve`);
    return response.data;
  },

  reject: async (employmentId: string): Promise<Employment> => {
    const response = await apiClient.patch<Employment>(`/employments/${employmentId}/reject`);
    return response.data;
  },

  cancel: async (employmentId: string): Promise<void> => {
    await apiClient.delete(`/employments/${employmentId}/cancel`);
  },

  getInstructors: async (): Promise<Instructor[]> => {
    const response = await apiClient.get<Instructor[]>('/employments/instructors');
    return response.data;
  },

  getInstructorDetail: async (id: string): Promise<InstructorDetail> => {
    const response = await apiClient.get<InstructorDetail>(`/employments/instructors/${id}`);
    return response.data;
  },

  updatePermissions: async (id: string, permissions: string[]): Promise<InstructorDetail> => {
    const response = await apiClient.patch<InstructorDetail>(
      `/employments/instructors/${id}/permissions`,
      { permissions },
    );
    return response.data;
  },

  resetPermissions: async (id: string): Promise<InstructorDetail> => {
    const response = await apiClient.post<InstructorDetail>(
      `/employments/instructors/${id}/permissions/reset`,
    );
    return response.data;
  },

  updateInstructorStatus: async (
    id: string,
    status: string,
    reason?: string,
  ): Promise<InstructorDetail> => {
    const response = await apiClient.patch<InstructorDetail>(
      `/employments/instructors/${id}/status`,
      { status, reason },
    );
    return response.data;
  },
};
