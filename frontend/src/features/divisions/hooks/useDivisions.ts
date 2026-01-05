import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { divisionApi } from '../../../services/divisionApi';
import type { DivisionCreateReq, DivisionUpdateReq, DivisionReorderReq } from '../types';

export const divisionKeys = {
  all: (dojangId: string) => ['divisions', dojangId] as const,
  list: (dojangId: string, sectionId: string) =>
    [...divisionKeys.all(dojangId), 'list', sectionId] as const,
  detail: (dojangId: string, divisionId: string) =>
    [...divisionKeys.all(dojangId), 'detail', divisionId] as const,
};

export const useDivisions = (dojangId: string, sectionId: string | null) => {
  return useQuery({
    queryKey: divisionKeys.list(dojangId, sectionId ?? ''),
    queryFn: () => divisionApi.getDivisions(dojangId, sectionId!),
    enabled: !!dojangId && !!sectionId,
    staleTime: 5 * 60 * 1000,
  });
};

export const useDivisionDetail = (dojangId: string, divisionId: string | null) => {
  return useQuery({
    queryKey: divisionKeys.detail(dojangId, divisionId ?? ''),
    queryFn: () => divisionApi.getDivisionDetail(dojangId, divisionId!),
    enabled: !!dojangId && !!divisionId,
    staleTime: 5 * 60 * 1000,
  });
};

export const useCreateDivision = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: DivisionCreateReq) => divisionApi.createDivision(dojangId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: divisionKeys.all(dojangId) });
    },
  });
};

export const useUpdateDivision = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ divisionId, data }: { divisionId: string; data: DivisionUpdateReq }) =>
      divisionApi.updateDivision(dojangId, divisionId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: divisionKeys.all(dojangId) });
    },
  });
};

export const useDeleteDivision = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (divisionId: string) => divisionApi.deleteDivision(dojangId, divisionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: divisionKeys.all(dojangId) });
    },
  });
};

export const useReorderDivisions = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: DivisionReorderReq) => divisionApi.reorderDivisions(dojangId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: divisionKeys.all(dojangId) });
    },
  });
};
