import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { divisionApi } from '../../../services/divisionApi';
import type { BulkEnrollmentReq } from '../types';
import { divisionKeys } from './useDivisions';

export const enrollmentKeys = {
  all: (dojangId: string) => ['enrollments', dojangId] as const,
  byDivision: (dojangId: string, divisionId: string) =>
    [...enrollmentKeys.all(dojangId), 'division', divisionId] as const,
};

export const useStudentsByDivision = (dojangId: string, divisionId: string | null) => {
  return useQuery({
    queryKey: enrollmentKeys.byDivision(dojangId, divisionId ?? ''),
    queryFn: () => divisionApi.getStudentsByDivision(dojangId, divisionId!),
    enabled: !!dojangId && !!divisionId,
    staleTime: 5 * 60 * 1000,
  });
};

export const useEnrollStudent = (dojangId: string, divisionId: string | null) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (studentId: string) =>
      divisionApi.enrollStudent(dojangId, divisionId!, studentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: enrollmentKeys.all(dojangId) });
      queryClient.invalidateQueries({ queryKey: divisionKeys.all(dojangId) });
    },
  });
};

export const useUnenrollStudent = (dojangId: string, divisionId: string | null) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (studentId: string) =>
      divisionApi.unenrollStudent(dojangId, divisionId!, studentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: enrollmentKeys.all(dojangId) });
      queryClient.invalidateQueries({ queryKey: divisionKeys.all(dojangId) });
    },
  });
};

export const useBulkEnrollStudents = (dojangId: string, divisionId: string | null) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: BulkEnrollmentReq) =>
      divisionApi.bulkEnrollStudents(dojangId, divisionId!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: enrollmentKeys.all(dojangId) });
      queryClient.invalidateQueries({ queryKey: divisionKeys.all(dojangId) });
    },
  });
};
