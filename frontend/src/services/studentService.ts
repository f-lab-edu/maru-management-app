import apiClient from './api';
import type {
  Student,
  StudentListResponse,
  StudentCreateRequest,
  StudentUpdateRequest,
  GuardianCreateRequest,
  GuardianUpdateRequest,
  Guardian,
} from '../types/student';

const BASE_PATH = '/students';

export interface StudentFilterParams {
  sectionId?: string;
  divisionId?: string;
  includeWithdrawn?: boolean;
}

export const studentService = {
  getStudents: async (
    dojangId: string,
    filters?: StudentFilterParams
  ): Promise<StudentListResponse> => {
    const response = await apiClient.get<StudentListResponse>(BASE_PATH, {
      params: { dojangId, ...filters },
    });
    return response.data;
  },

  getStudent: async (dojangId: string, studentId: string): Promise<Student> => {
    const response = await apiClient.get<Student>(`${BASE_PATH}/${studentId}`, {
      params: { dojangId },
    });
    return response.data;
  },

  createStudent: async (dojangId: string, data: StudentCreateRequest): Promise<Student> => {
    const response = await apiClient.post<Student>(BASE_PATH, data, {
      params: { dojangId },
    });
    return response.data;
  },

  updateStudent: async (
    dojangId: string,
    studentId: string,
    data: StudentUpdateRequest
  ): Promise<Student> => {
    const response = await apiClient.patch<Student>(`${BASE_PATH}/${studentId}`, data, {
      params: { dojangId },
    });
    return response.data;
  },

  deleteStudent: async (dojangId: string, studentId: string): Promise<void> => {
    await apiClient.delete(`${BASE_PATH}/${studentId}`, {
      params: { dojangId },
    });
  },

  addGuardian: async (
    dojangId: string,
    studentId: string,
    data: GuardianCreateRequest
  ): Promise<Guardian> => {
    const response = await apiClient.post<Guardian>(
      `${BASE_PATH}/${studentId}/guardians`,
      data,
      { params: { dojangId } }
    );
    return response.data;
  },

  setPrimaryGuardian: async (
    dojangId: string,
    studentId: string,
    guardianId: string
  ): Promise<void> => {
    await apiClient.patch(
      `${BASE_PATH}/${studentId}/guardians/${guardianId}/primary`,
      {},
      { params: { dojangId } }
    );
  },

  bulkDelete: async (dojangId: string, studentIds: string[]): Promise<void> => {
    await apiClient.delete(`${BASE_PATH}/bulk`, {
      params: { dojangId },
      data: { studentIds },
    });
  },

  updateGuardian: async (
    dojangId: string,
    studentId: string,
    guardianId: string,
    data: GuardianUpdateRequest
  ): Promise<Guardian> => {
    const response = await apiClient.patch<Guardian>(
      `${BASE_PATH}/${studentId}/guardians/${guardianId}`,
      data,
      { params: { dojangId } }
    );
    return response.data;
  },

  deleteGuardian: async (
    dojangId: string,
    studentId: string,
    guardianId: string
  ): Promise<void> => {
    await apiClient.delete(`${BASE_PATH}/${studentId}/guardians/${guardianId}`, {
      params: { dojangId },
    });
  },

  restoreStudent: async (dojangId: string, studentId: string): Promise<Student> => {
    const response = await apiClient.patch<Student>(`${BASE_PATH}/${studentId}/restore`, null, {
      params: { dojangId },
    });
    return response.data;
  },
};
