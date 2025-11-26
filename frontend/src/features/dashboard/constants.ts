import { Applicant, CalendarEvent, NotificationItem } from './types';

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

export const APPLICANTS: Applicant[] = [
  { name: '최민수', age: '8세', phone: '010-1234-5678', status: '상담대기' },
  { name: '김지아', age: '10세', phone: '010-9876-5432', status: '방문예정' },
  { name: '박서준', age: '7세', phone: '010-5555-4444', status: '신규문의' },
  { name: '이서윤', age: '9세', phone: '010-1111-2222', status: '상담대기' },
  { name: '정우성', age: '11세', phone: '010-3333-4444', status: '신규문의' },
];

export const MOCK_DASHBOARD_STATS: import('./types').DashboardStats = {
  totalStudents: 127,
  attendanceRate: 89,
  monthlyRevenue: 12500000,
  revenueTarget: 15000000,
  activeStudents: 118,
};
