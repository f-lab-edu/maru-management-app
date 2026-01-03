import apiClient from './api';
import type {
  DivisionListRes,
  DivisionRes,
  DivisionDetailRes,
  DivisionCreateReq,
  DivisionUpdateReq,
  DivisionReorderReq,
  EnrolledStudentListRes,
  BulkEnrollmentReq,
  BulkEnrollmentRes,
} from '../features/divisions/types';

const BASE_PATH = '/divisions';

export const divisionApi = {
  getDivisions: async (dojangId: string, sectionId: string): Promise<DivisionListRes> => {
    const response = await apiClient.get<DivisionListRes>(BASE_PATH, {
      params: { dojangId, sectionId },
    });
    return response.data;
  },

  getDivisionDetail: async (dojangId: string, divisionId: string): Promise<DivisionDetailRes> => {
    const response = await apiClient.get<DivisionDetailRes>(`${BASE_PATH}/${divisionId}`, {
      params: { dojangId },
    });
    return response.data;
  },

  createDivision: async (dojangId: string, data: DivisionCreateReq): Promise<DivisionRes> => {
    const response = await apiClient.post<DivisionRes>(BASE_PATH, data, {
      params: { dojangId },
    });
    return response.data;
  },

  updateDivision: async (
    dojangId: string,
    divisionId: string,
    data: DivisionUpdateReq
  ): Promise<DivisionRes> => {
    const response = await apiClient.patch<DivisionRes>(`${BASE_PATH}/${divisionId}`, data, {
      params: { dojangId },
    });
    return response.data;
  },

  deleteDivision: async (dojangId: string, divisionId: string): Promise<void> => {
    await apiClient.delete(`${BASE_PATH}/${divisionId}`, {
      params: { dojangId },
    });
  },

  reorderDivisions: async (dojangId: string, data: DivisionReorderReq): Promise<void> => {
    await apiClient.patch(`${BASE_PATH}/reorder`, data, {
      params: { dojangId },
    });
  },

  getStudentsByDivision: async (
    dojangId: string,
    divisionId: string
  ): Promise<EnrolledStudentListRes> => {
    const response = await apiClient.get<EnrolledStudentListRes>('/enrollments', {
      params: { dojangId, divisionId },
    });
    return response.data;
  },

  enrollStudent: async (
    dojangId: string,
    divisionId: string,
    studentId: string
  ): Promise<void> => {
    await apiClient.post('/enrollments', { studentId }, {
      params: { dojangId, divisionId },
    });
  },

  unenrollStudent: async (
    dojangId: string,
    divisionId: string,
    studentId: string
  ): Promise<void> => {
    await apiClient.delete(`/enrollments/${studentId}`, {
      params: { dojangId, divisionId },
    });
  },

  bulkEnrollStudents: async (
    dojangId: string,
    divisionId: string,
    data: BulkEnrollmentReq
  ): Promise<BulkEnrollmentRes> => {
    const response = await apiClient.post<BulkEnrollmentRes>('/enrollments/bulk', data, {
      params: { dojangId, divisionId },
    });
    return response.data;
  },
};
