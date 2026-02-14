import apiClient from './api';
import { NOTIFICATION_PAGE_SIZE } from '../features/messages/constants';
import type {
  NotificationDailySummary,
  NotificationDetail,
  PagedResult,
} from '../types/message';

const BASE_PATH = '/notifications';

export const notificationApi = {
  getSummary: async (dojangId: string, page = 0, size = NOTIFICATION_PAGE_SIZE): Promise<PagedResult<NotificationDailySummary>> => {
    const response = await apiClient.get<PagedResult<NotificationDailySummary>>(
      `${BASE_PATH}/summary`,
      { params: { dojangId, page, size } },
    );
    return response.data;
  },

  getDetails: async (
    dojangId: string,
    date: string,
    messageType: string,
    page = 0,
    size = NOTIFICATION_PAGE_SIZE,
  ): Promise<PagedResult<NotificationDetail>> => {
    const response = await apiClient.get<PagedResult<NotificationDetail>>(BASE_PATH, {
      params: { dojangId, date, messageType, page, size },
    });
    return response.data;
  },
};
