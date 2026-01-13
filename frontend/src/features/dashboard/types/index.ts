export interface DashboardStats {
  totalStudents: number;
  studentsDiff: number;
  attendanceRate: number;
  attendanceRateDiff: number;
  monthlyRevenue: number;
  revenueTarget: number;
  activeStudents: number;
  pausedStudents: number;
}

export type EventType = 'payment' | 'alert' | 'info';

export interface CalendarEvent {
  date: Date;
  type: EventType;
}

export interface NotificationItem {
  title: string;
  desc: string;
  time: string;
  type: EventType;
}

export interface RecentStudent {
  id: string;
  name: string;
  age: number | null;
  enrolledAt: string;
  photoUrl: string | null;
}
