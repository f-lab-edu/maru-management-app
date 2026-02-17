import apiClient from './api';
import { BROADCAST_PAGE_SIZE, RECIPIENT_PAGE_SIZE } from '../features/messages/constants';
import type {
  BroadcastCreateRequest,
  BroadcastCreateResponse,
  BroadcastSummary,
  BroadcastDetail,
  RecipientPreviewRequest,
  RecipientPreview,
  RecipientStatus,
  ResendFailedResponse,
  PagedResult,
} from '../types/message';

const BASE_PATH = '/broadcasts';

export const broadcastApi = {
  create: async (dojangId: string, data: BroadcastCreateRequest): Promise<BroadcastCreateResponse> => {
    const response = await apiClient.post<BroadcastCreateResponse>(BASE_PATH, data, {
      params: { dojangId },
    });
    return response.data;
  },

  getList: async (dojangId: string, page = 0, size = BROADCAST_PAGE_SIZE): Promise<PagedResult<BroadcastSummary>> => {
    const response = await apiClient.get<PagedResult<BroadcastSummary>>(BASE_PATH, {
      params: { dojangId, page, size },
    });
    return response.data;
  },

  getDetail: async (dojangId: string, broadcastId: string): Promise<BroadcastDetail> => {
    const response = await apiClient.get<BroadcastDetail>(`${BASE_PATH}/${broadcastId}`, {
      params: { dojangId },
    });
    return response.data;
  },

  getRecipients: async (
    dojangId: string,
    broadcastId: string,
    page = 0,
    size = RECIPIENT_PAGE_SIZE,
    status?: string,
  ): Promise<PagedResult<RecipientStatus>> => {
    const response = await apiClient.get<PagedResult<RecipientStatus>>(
      `${BASE_PATH}/${broadcastId}/recipients`,
      { params: { dojangId, page, size, ...(status && { status }) } },
    );
    return response.data;
  },

  previewRecipients: async (dojangId: string, data: RecipientPreviewRequest): Promise<RecipientPreview> => {
    const response = await apiClient.post<RecipientPreview>(`${BASE_PATH}/preview-recipients`, data, {
      params: { dojangId },
    });
    return response.data;
  },

  resendFailed: async (dojangId: string, broadcastId: string): Promise<ResendFailedResponse> => {
    const response = await apiClient.post<ResendFailedResponse>(
      `${BASE_PATH}/${broadcastId}/resend-failed`,
      null,
      { params: { dojangId } },
    );
    return response.data;
  },
};
