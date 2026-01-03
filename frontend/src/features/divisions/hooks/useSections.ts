import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { sectionApi } from '../../../services/sectionApi';
import type { SectionCreateReq, SectionUpdateReq, SectionReorderReq } from '../types';

export const SECTIONS_QUERY_KEY = 'sections';
export const DIVISIONS_QUERY_KEY = 'divisions';

export const useSections = (dojangId: string) => {
  return useQuery({
    queryKey: [SECTIONS_QUERY_KEY, dojangId],
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
      queryClient.invalidateQueries({ queryKey: [SECTIONS_QUERY_KEY] });
    },
  });
};

export const useUpdateSection = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ sectionId, data }: { sectionId: string; data: SectionUpdateReq }) =>
      sectionApi.updateSection(dojangId, sectionId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [SECTIONS_QUERY_KEY] });
    },
  });
};

export const useDeleteSection = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (sectionId: string) => sectionApi.deleteSection(dojangId, sectionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [SECTIONS_QUERY_KEY] });
      queryClient.invalidateQueries({ queryKey: [DIVISIONS_QUERY_KEY] });
    },
  });
};

export const useReorderSections = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: SectionReorderReq) => sectionApi.reorderSections(dojangId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [SECTIONS_QUERY_KEY] });
    },
  });
};
