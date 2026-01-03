import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import { studentService, type StudentFilterParams } from '@/services/studentService';
import type { StudentCreateRequest, StudentUpdateRequest } from '@/types/student';

const QUERY_KEYS = {
  students: (dojangId: string, filters?: StudentFilterParams) =>
    ['students', dojangId, filters] as const,
  student: (dojangId: string, studentId: string) => ['student', dojangId, studentId] as const,
};

export function useStudents(dojangId: string | null, filters?: StudentFilterParams) {
  return useQuery({
    queryKey: QUERY_KEYS.students(dojangId ?? '', filters),
    queryFn: () => studentService.getStudents(dojangId!, filters),
    enabled: !!dojangId,
    placeholderData: keepPreviousData,
  });
}

export function useStudent(dojangId: string | null, studentId: string | null) {
  return useQuery({
    queryKey: QUERY_KEYS.student(dojangId ?? '', studentId ?? ''),
    queryFn: () => studentService.getStudent(dojangId!, studentId!),
    enabled: !!dojangId && !!studentId,
  });
}

export function useCreateStudent(dojangId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: StudentCreateRequest) => studentService.createStudent(dojangId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students(dojangId) });
    },
  });
}

export function useUpdateStudent(dojangId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ studentId, data }: { studentId: string; data: StudentUpdateRequest }) =>
      studentService.updateStudent(dojangId, studentId, data),
    onSuccess: (_, { studentId }) => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students(dojangId) });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.student(dojangId, studentId) });
    },
  });
}

export function useDeleteStudent(dojangId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (studentId: string) => studentService.deleteStudent(dojangId, studentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students(dojangId) });
    },
  });
}

export function useBulkDeleteStudents(dojangId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (studentIds: string[]) => studentService.bulkDelete(dojangId, studentIds),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students(dojangId) });
    },
  });
}
