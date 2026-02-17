import apiClient from './api';
import type {
  MonthlyUsage,
  NotificationDetail,
  PagedResult,
} from '../types/message';

const BASE_PATH = '/notifications';

export const notificationApi = {
  getMonthlyUsage: async (dojangId: string, yearMonth: string): Promise<MonthlyUsage> => {
    const response = await apiClient.get<MonthlyUsage>(
      `${BASE_PATH}/monthly-usage`,
      { params: { dojangId, yearMonth } },
    );
    return response.data;
  },

  getTimeline: async (
    dojangId: string,
    date: string,
    messageType?: string,
    page = 0,
    size = 30,
  ): Promise<PagedResult<NotificationDetail>> => {
    const params: Record<string, unknown> = { dojangId, date, page, size };
    if (messageType) params.messageType = messageType;
    const response = await apiClient.get<PagedResult<NotificationDetail>>(
      `${BASE_PATH}/timeline`,
      { params },
    );
    return response.data;
  },
};
