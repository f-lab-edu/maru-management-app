import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { sectionApi } from '../../../services/sectionApi';
import type { SectionCreateReq, SectionUpdateReq, SectionReorderReq } from '../types';
import { divisionKeys } from './useDivisions';

export const sectionKeys = {
  all: (dojangId: string) => ['sections', dojangId] as const,
  list: (dojangId: string) => [...sectionKeys.all(dojangId), 'list'] as const,
};

export const useSections = (dojangId: string) => {
  return useQuery({
    queryKey: sectionKeys.list(dojangId),
    queryFn: () => sectionApi.getSections(dojangId),
    enabled: !!dojangId,
    staleTime: 5 * 60 * 1000,
  });
};

export const useCreateSection = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: SectionCreateReq) => sectionApi.createSection(dojangId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: sectionKeys.all(dojangId) });
    },
  });
};

export const useUpdateSection = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ sectionId, data }: { sectionId: string; data: SectionUpdateReq }) =>
      sectionApi.updateSection(dojangId, sectionId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: sectionKeys.all(dojangId) });
    },
  });
};

export const useDeleteSection = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (sectionId: string) => sectionApi.deleteSection(dojangId, sectionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: sectionKeys.all(dojangId) });
      queryClient.invalidateQueries({ queryKey: divisionKeys.all(dojangId) });
    },
  });
};

export const useReorderSections = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: SectionReorderReq) => sectionApi.reorderSections(dojangId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: sectionKeys.all(dojangId) });
    },
  });
};
