import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { studentService } from '@/services/studentService';
import type { StudentCreateRequest, StudentUpdateRequest } from '@/types/student';

const QUERY_KEYS = {
  students: (dojangId: number) => ['students', dojangId] as const,
  student: (dojangId: number, studentId: number) => ['student', dojangId, studentId] as const,
};

export function useStudents(dojangId: number | null) {
  return useQuery({
    queryKey: QUERY_KEYS.students(dojangId ?? 0),
    queryFn: () => studentService.getStudents(dojangId!),
    enabled: !!dojangId,
  });
}

export function useStudent(dojangId: number | null, studentId: number | null) {
  return useQuery({
    queryKey: QUERY_KEYS.student(dojangId ?? 0, studentId ?? 0),
    queryFn: () => studentService.getStudent(dojangId!, studentId!),
    enabled: !!dojangId && !!studentId,
  });
}

export function useCreateStudent(dojangId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: StudentCreateRequest) => studentService.createStudent(dojangId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students(dojangId) });
    },
  });
}

export function useUpdateStudent(dojangId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ studentId, data }: { studentId: number; data: StudentUpdateRequest }) =>
      studentService.updateStudent(dojangId, studentId, data),
    onSuccess: (_, { studentId }) => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students(dojangId) });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.student(dojangId, studentId) });
    },
  });
}

export function useDeleteStudent(dojangId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (studentId: number) => studentService.deleteStudent(dojangId, studentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students(dojangId) });
    },
  });
}

export function useBulkDeleteStudents(dojangId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (studentIds: number[]) => studentService.bulkDelete(dojangId, studentIds),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.students(dojangId) });
    },
  });
}
