import { useQuery } from '@tanstack/react-query';
import { notificationApi } from '../../../services/notificationApi';

export const notificationKeys = {
  all: (dojangId: string) => ['notifications', dojangId] as const,
  summary: (dojangId: string, page: number) => [...notificationKeys.all(dojangId), 'summary', page] as const,
  details: (dojangId: string, date: string, type: string, page: number) =>
    [...notificationKeys.all(dojangId), 'details', date, type, page] as const,
};

export const useNotificationSummary = (dojangId: string, page = 0) => {
  return useQuery({
    queryKey: notificationKeys.summary(dojangId, page),
    queryFn: () => notificationApi.getSummary(dojangId, page),
    enabled: !!dojangId,
  });
};

export const useNotificationDetails = (dojangId: string, date: string, messageType: string, page = 0) => {
  return useQuery({
    queryKey: notificationKeys.details(dojangId, date, messageType, page),
    queryFn: () => notificationApi.getDetails(dojangId, date, messageType, page),
    enabled: !!dojangId && !!date && !!messageType,
  });
};
