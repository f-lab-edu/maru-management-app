export interface DashboardStats {
  totalStudents: number;
  attendanceRate: number;
  monthlyRevenue: number;
  revenueTarget: number;
  activeStudents: number;
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

export interface Applicant {
  name: string;
  age: string;
  phone: string;
  status: string;
}
