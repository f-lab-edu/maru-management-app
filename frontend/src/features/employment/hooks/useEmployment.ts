import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { employmentService } from '../../../services/employmentService';

export const employmentKeys = {
  all: ['employment'] as const,
  search: (keyword: string, page: number) => [...employmentKeys.all, 'search', keyword, page] as const,
  myRequests: () => [...employmentKeys.all, 'myRequests'] as const,
  pendingRequests: () => [...employmentKeys.all, 'pendingRequests'] as const,
};

export function useDojangSearch(keyword: string, page: number = 0, enabled: boolean = true) {
  const queryClient = useQueryClient();
  const isEnabled = enabled && (keyword.length === 0 || keyword.length >= 2);

  const query = useQuery({
    queryKey: employmentKeys.search(keyword, page),
    queryFn: () => employmentService.searchDojangs(keyword, page),
    enabled: isEnabled,
    staleTime: 1000 * 60 * 5,
  });

  const totalPages = query.data?.totalPages ?? 0;

  useEffect(() => {
    if (!isEnabled || totalPages === 0) return;
    if (page >= totalPages) return;

    if (page < totalPages - 1) {
      queryClient.prefetchQuery({
        queryKey: employmentKeys.search(keyword, page + 1),
        queryFn: () => employmentService.searchDojangs(keyword, page + 1),
        staleTime: 1000 * 60 * 5,
      });
    }
  }, [queryClient, keyword, page, isEnabled, totalPages]);

  return query;
}

export function useMyRequests() {
  return useQuery({
    queryKey: employmentKeys.myRequests(),
    queryFn: () => employmentService.getMyRequests(),
  });
}

export function usePendingRequests() {
  return useQuery({
    queryKey: employmentKeys.pendingRequests(),
    queryFn: () => employmentService.getPendingRequests(),
  });
}

export function useRequestApproval() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (dojangId: number) => employmentService.requestApproval(dojangId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: employmentKeys.myRequests() });
    },
  });
}

export function useApproveRequest() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (employmentId: number) => employmentService.approve(employmentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: employmentKeys.pendingRequests() });
    },
  });
}

export function useRejectRequest() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (employmentId: number) => employmentService.reject(employmentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: employmentKeys.pendingRequests() });
    },
  });
}

export function useCancelRequest() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (employmentId: number) => employmentService.cancel(employmentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: employmentKeys.myRequests() });
    },
  });
}
