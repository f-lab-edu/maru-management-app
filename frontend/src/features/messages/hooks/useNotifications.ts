import { useQuery } from '@tanstack/react-query';
import { notificationApi } from '../../../services/notificationApi';

export const notificationKeys = {
  all: (dojangId: string) => ['notifications', dojangId] as const,
  timeline: (dojangId: string, date: string, messageType?: string, page?: number) =>
    [...notificationKeys.all(dojangId), 'timeline', date, messageType, page] as const,
  monthlyUsage: (dojangId: string, yearMonth: string) =>
    [...notificationKeys.all(dojangId), 'monthly-usage', yearMonth] as const,
};

export const useMonthlyUsage = (dojangId: string, yearMonth: string) => {
  return useQuery({
    queryKey: notificationKeys.monthlyUsage(dojangId, yearMonth),
    queryFn: () => notificationApi.getMonthlyUsage(dojangId, yearMonth),
    enabled: !!dojangId && !!yearMonth,
    staleTime: 60_000,
  });
};

export const useNotificationTimeline = (
  dojangId: string, date: string, messageType?: string, page = 0,
) => {
  return useQuery({
    queryKey: notificationKeys.timeline(dojangId, date, messageType, page),
    queryFn: () => notificationApi.getTimeline(dojangId, date, messageType, page),
    enabled: !!dojangId && !!date,
    staleTime: 30_000,
  });
};
