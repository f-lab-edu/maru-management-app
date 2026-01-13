import { useQuery } from '@tanstack/react-query';
import { dashboardService } from '@/services/dashboardService';

export const dashboardKeys = {
  all: (dojangId: string) => ['dashboard', dojangId] as const,
  summary: (dojangId: string) => [...dashboardKeys.all(dojangId), 'summary'] as const,
  notifications: (dojangId: string, limit: number, offset: number) =>
    [...dashboardKeys.all(dojangId), 'notifications', { limit, offset }] as const,
  recentStudents: (dojangId: string, limit: number, offset: number) =>
    [...dashboardKeys.all(dojangId), 'recentStudents', { limit, offset }] as const,
};

export function useDashboardSummary(dojangId: string | null, enabled = true) {
  return useQuery({
    queryKey: dashboardKeys.summary(dojangId ?? ''),
    queryFn: () => dashboardService.getSummary(dojangId!),
    enabled: !!dojangId && enabled,
  });
}

export function useDashboardNotifications(
  dojangId: string | null,
  limit = 10,
  offset = 0
) {
  return useQuery({
    queryKey: dashboardKeys.notifications(dojangId ?? '', limit, offset),
    queryFn: () => dashboardService.getNotifications(dojangId!, limit, offset),
    enabled: !!dojangId,
  });
}

export function useRecentStudents(
  dojangId: string | null,
  limit = 10,
  offset = 0
) {
  return useQuery({
    queryKey: dashboardKeys.recentStudents(dojangId ?? '', limit, offset),
    queryFn: () => dashboardService.getRecentStudents(dojangId!, limit, offset),
    enabled: !!dojangId,
  });
}
