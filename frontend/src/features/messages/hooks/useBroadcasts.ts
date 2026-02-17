import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { broadcastApi } from '../../../services/broadcastApi';
import { RECIPIENT_PAGE_SIZE } from '../constants';
import type { BroadcastCreateRequest, RecipientPreviewRequest } from '../../../types/message';

export const broadcastKeys = {
  all: (dojangId: string) => ['broadcasts', dojangId] as const,
  list: (dojangId: string, page: number) => [...broadcastKeys.all(dojangId), 'list', page] as const,
  detail: (dojangId: string, broadcastId: string) => [...broadcastKeys.all(dojangId), 'detail', broadcastId] as const,
  recipients: (dojangId: string, broadcastId: string, page: number, status?: string) =>
    [...broadcastKeys.all(dojangId), 'recipients', broadcastId, page, status] as const,
};

export const useBroadcasts = (dojangId: string, page = 0) => {
  return useQuery({
    queryKey: broadcastKeys.list(dojangId, page),
    queryFn: () => broadcastApi.getList(dojangId, page),
    enabled: !!dojangId,
  });
};

export const useBroadcastDetail = (dojangId: string, broadcastId: string) => {
  return useQuery({
    queryKey: broadcastKeys.detail(dojangId, broadcastId),
    queryFn: () => broadcastApi.getDetail(dojangId, broadcastId),
    enabled: !!dojangId && !!broadcastId,
  });
};

export const useBroadcastRecipients = (dojangId: string, broadcastId: string, page = 0, status?: string) => {
  return useQuery({
    queryKey: broadcastKeys.recipients(dojangId, broadcastId, page, status),
    queryFn: () => broadcastApi.getRecipients(dojangId, broadcastId, page, RECIPIENT_PAGE_SIZE, status || undefined),
    enabled: !!dojangId && !!broadcastId,
  });
};

export const useCreateBroadcast = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: BroadcastCreateRequest) => broadcastApi.create(dojangId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: broadcastKeys.all(dojangId) });
    },
  });
};

export const usePreviewRecipients = (dojangId: string) => {
  return useMutation({
    mutationFn: (data: RecipientPreviewRequest) => broadcastApi.previewRecipients(dojangId, data),
  });
};

export const useResendFailed = (dojangId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (broadcastId: string) => broadcastApi.resendFailed(dojangId, broadcastId),
    onSuccess: (_data, broadcastId) => {
      queryClient.invalidateQueries({ queryKey: broadcastKeys.detail(dojangId, broadcastId) });
      queryClient.invalidateQueries({ queryKey: broadcastKeys.all(dojangId) });
    },
  });
};
