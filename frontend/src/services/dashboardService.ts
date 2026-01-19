import apiClient from './api';

const BASE_PATH = '/dashboard';

export interface DashboardSummaryRes {
  currentlyAttendingCount: number;
  totalStudents: number;
  studentsDiff: number;
  attendanceRate: number;
  attendanceRateDiff: number;
  monthlyRevenue: number;
  revenueTarget: number;
  activeStudents: number;
  pausedStudents: number;
}

export interface NotificationItemRes {
  type: 'PAYMENT' | 'INSTRUCTOR' | 'STUDENT';
  title: string;
  description: string;
  occurredAt: string;
}

export interface DashboardNotificationRes {
  items: NotificationItemRes[];
  hasMore: boolean;
}

export interface RecentStudentItemRes {
  id: string;
  name: string;
  enrolledAt: string;
  status: string;
  photoUrl: string | null;
  age: number | null;
}

export interface RecentStudentRes {
  students: RecentStudentItemRes[];
  hasMore: boolean;
}

export const dashboardService = {
  getSummary: async (dojangId: string): Promise<DashboardSummaryRes> => {
    const response = await apiClient.get<DashboardSummaryRes>(`${BASE_PATH}/summary`, {
      params: { dojangId },
    });
    return response.data;
  },

  getNotifications: async (
    dojangId: string,
    limit = 10,
    offset = 0
  ): Promise<DashboardNotificationRes> => {
    const response = await apiClient.get<DashboardNotificationRes>(`${BASE_PATH}/notifications`, {
      params: { dojangId, limit, offset },
    });
    return response.data;
  },

  getRecentStudents: async (
    dojangId: string,
    limit = 10,
    offset = 0
  ): Promise<RecentStudentRes> => {
    const response = await apiClient.get<RecentStudentRes>(`${BASE_PATH}/recent-students`, {
      params: { dojangId, limit, offset },
    });
    return response.data;
  },
};
