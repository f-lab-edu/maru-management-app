import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { divisionApi } from '../../../services/divisionApi';
import type { BulkEnrollmentReq } from '../types';

const ENROLLED_STUDENTS_QUERY_KEY = 'enrolledStudents';
const DIVISIONS_QUERY_KEY = 'divisions';

export const useStudentsByDivision = (dojangId: string, divisionId: string | null) => {
  return useQuery({
    queryKey: [ENROLLED_STUDENTS_QUERY_KEY, dojangId, divisionId],
    queryFn: () => divisionApi.getStudentsByDivision(dojangId, divisionId!),
    enabled: !!dojangId && !!divisionId,
    staleTime: 5 * 60 * 1000,
  });
};

export const useEnrollStudent = (dojangId: string, divisionId: string | null, sectionId: string | null) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (studentId: string) =>
      divisionApi.enrollStudent(dojangId, divisionId!, studentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ENROLLED_STUDENTS_QUERY_KEY, dojangId, divisionId] });
      queryClient.invalidateQueries({ queryKey: [DIVISIONS_QUERY_KEY, dojangId, sectionId] });
    },
  });
};

export const useUnenrollStudent = (dojangId: string, divisionId: string | null, sectionId: string | null) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (studentId: string) =>
      divisionApi.unenrollStudent(dojangId, divisionId!, studentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ENROLLED_STUDENTS_QUERY_KEY, dojangId, divisionId] });
      queryClient.invalidateQueries({ queryKey: [DIVISIONS_QUERY_KEY, dojangId, sectionId] });
    },
  });
};

export const useBulkEnrollStudents = (dojangId: string, divisionId: string | null, sectionId: string | null) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: BulkEnrollmentReq) =>
      divisionApi.bulkEnrollStudents(dojangId, divisionId!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ENROLLED_STUDENTS_QUERY_KEY, dojangId, divisionId] });
      queryClient.invalidateQueries({ queryKey: [DIVISIONS_QUERY_KEY, dojangId, sectionId] });
    },
  });
};
