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

export const studentService = {
  getStudents: async (dojangId: number): Promise<StudentListResponse> => {
    const response = await apiClient.get<StudentListResponse>(BASE_PATH, {
      params: { dojangId },
    });
    return response.data;
  },

  getStudent: async (dojangId: number, studentId: number): Promise<Student> => {
    const response = await apiClient.get<Student>(`${BASE_PATH}/${studentId}`, {
      params: { dojangId },
    });
    return response.data;
  },

  createStudent: async (dojangId: number, data: StudentCreateRequest): Promise<Student> => {
    const response = await apiClient.post<Student>(BASE_PATH, data, {
      params: { dojangId },
    });
    return response.data;
  },

  updateStudent: async (
    dojangId: number,
    studentId: number,
    data: StudentUpdateRequest
  ): Promise<Student> => {
    const response = await apiClient.patch<Student>(`${BASE_PATH}/${studentId}`, data, {
      params: { dojangId },
    });
    return response.data;
  },

  deleteStudent: async (dojangId: number, studentId: number): Promise<void> => {
    await apiClient.delete(`${BASE_PATH}/${studentId}`, {
      params: { dojangId },
    });
  },

  addGuardian: async (
    dojangId: number,
    studentId: number,
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
    dojangId: number,
    studentId: number,
    guardianId: number
  ): Promise<void> => {
    await apiClient.patch(
      `${BASE_PATH}/${studentId}/guardians/${guardianId}/primary`,
      {},
      { params: { dojangId } }
    );
  },

  bulkDelete: async (dojangId: number, studentIds: number[]): Promise<void> => {
    await apiClient.delete(`${BASE_PATH}/bulk`, {
      params: { dojangId },
      data: { studentIds },
    });
  },

  updateGuardian: async (
    dojangId: number,
    studentId: number,
    guardianId: number,
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
    dojangId: number,
    studentId: number,
    guardianId: number
  ): Promise<void> => {
    await apiClient.delete(`${BASE_PATH}/${studentId}/guardians/${guardianId}`, {
      params: { dojangId },
    });
  },
};
