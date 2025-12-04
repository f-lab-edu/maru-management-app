import apiClient from './api';
import { DojangSearchResult, Employment, PendingApprovalRequest } from '../types/employment';

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
    size: number = 10,
    strategy: string = 'MEMORY'
  ): Promise<PagedResult<DojangSearchResult>> => {
    const response = await apiClient.get<PagedResult<DojangSearchResult>>('/dojangs/search', {
      params: { keyword, page, size, strategy },
    });
    return response.data;
  },

  requestApproval: async (dojangId: number): Promise<Employment> => {
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

  approve: async (employmentId: number): Promise<Employment> => {
    const response = await apiClient.patch<Employment>(`/employments/${employmentId}/approve`);
    return response.data;
  },

  reject: async (employmentId: number): Promise<Employment> => {
    const response = await apiClient.patch<Employment>(`/employments/${employmentId}/reject`);
    return response.data;
  },

  cancel: async (employmentId: number): Promise<void> => {
    await apiClient.delete(`/employments/${employmentId}/cancel`);
  },
};
