import { CalendarEvent, NotificationItem, RecentStudent } from './types';

export const EVENTS: CalendarEvent[] = [
  { date: new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() - 2), type: 'payment' },
  { date: new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() + 3), type: 'alert' },
  { date: new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() + 3), type: 'info' },
  { date: new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() + 5), type: 'info' },
  { date: new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() + 12), type: 'payment' },
  { date: new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() + 12), type: 'alert' },
];

export const NOTIFICATIONS: NotificationItem[] = [
  { title: '박영희 원생 수납 완료', desc: '11월 수련비 결제가 완료되었습니다.', time: '2시간 전', type: 'payment' },
  { title: '김철수 사범 승인 요청', desc: '새로운 사범 등록 승인 요청이 있습니다.', time: '3시간 전', type: 'alert' },
  { title: '승급 심사 일정 안내', desc: '다음 주 금요일 정기 승급 심사가 있습니다.', time: '5시간 전', type: 'info' },
  { title: '신규 상담 예약', desc: '이민준 학부모님 상담 예약이 있습니다.', time: '6시간 전', type: 'info' },
  { title: '차량 운행 일지 미작성', desc: '어제 날짜 차량 운행 일지가 작성되지 않았습니다.', time: '1일 전', type: 'alert' },
];

export const RECENT_STUDENTS: RecentStudent[] = [
  { id: '1', name: '최민수', age: 8, enrolledAt: '1월 5일', photoUrl: null },
  { id: '2', name: '김지아', age: 10, enrolledAt: '1월 3일', photoUrl: null },
  { id: '3', name: '박서준', age: 7, enrolledAt: '1월 2일', photoUrl: null },
  { id: '4', name: '이서윤', age: 9, enrolledAt: '12월 28일', photoUrl: null },
  { id: '5', name: '정우성', age: 11, enrolledAt: '12월 25일', photoUrl: null },
];

export const MOCK_DASHBOARD_STATS: import('./types').DashboardStats = {
  totalStudents: 127,
  studentsDiff: 2,
  attendanceRate: 89,
  attendanceRateDiff: 5,
  monthlyRevenue: 12500000,
  revenueTarget: 15000000,
  activeStudents: 118,
  pausedStudents: 9,
};
