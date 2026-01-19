import { Calendar, Clock, TrendingUp } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';

interface AttendanceHistoryTabProps {
  studentId: string;
}

interface AttendanceRecord {
  id: string;
  date: string;
  checkIn?: string;
  checkOut?: string;
}

// TODO: API 연동 시 실제 데이터로 교체
const MOCK_ATTENDANCE: AttendanceRecord[] = [
  { id: '1', date: '2024-12-07', checkIn: '16:30', checkOut: '18:00' },
  { id: '2', date: '2024-12-05', checkIn: '16:25', checkOut: '17:55' },
  { id: '3', date: '2024-12-03', checkIn: '16:35', checkOut: '18:05' },
  { id: '4', date: '2024-12-01', checkIn: '16:30', checkOut: '18:00' },
  { id: '5', date: '2024-11-29', checkIn: '16:20', checkOut: '17:50' },
];

const MOCK_STATS = {
  monthlyRate: 85,
  totalDays: 12,
  attendedDays: 10,
};

export function AttendanceHistoryTab({ studentId }: AttendanceHistoryTabProps) {
  // TODO: useQuery로 실제 API 호출
  const attendance = MOCK_ATTENDANCE;
  const stats = MOCK_STATS;
  console.log('출결이력 조회 - studentId:', studentId);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
      weekday: 'short',
    });
  };

  return (
    <div className="space-y-4">
      {/* 이번 달 출석률 */}
      <div className="rounded-lg bg-primary/5 p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <TrendingUp className="h-5 w-5 text-primary" />
            <span className="font-medium">이번 달 출석률</span>
          </div>
          <span className="text-2xl font-bold text-primary">{stats.monthlyRate}%</span>
        </div>
        <p className="mt-1 text-sm text-muted-foreground">
          총 {stats.totalDays}일 중 {stats.attendedDays}일 출석
        </p>
      </div>

      {/* 최근 출결 기록 */}
      <div className="space-y-2">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Calendar className="h-4 w-4" />
          <span>최근 5회 출결 기록</span>
        </div>

        <div className="space-y-2">
          {attendance.map((record) => (
            <div
              key={record.id}
              className="flex items-center justify-between rounded-lg border p-3"
            >
              <div>
                <p className="font-medium">{formatDate(record.date)}</p>
              </div>
              <div className="flex items-center gap-2">
                {record.checkIn && (
                  <Badge variant="outline" className="border-0 bg-blue-100 text-blue-800">
                    <Clock className="mr-1 h-3 w-3" />
                    {record.checkIn}
                  </Badge>
                )}
                {record.checkOut && (
                  <Badge variant="outline" className="border-0 bg-gray-100 text-gray-800">
                    <Clock className="mr-1 h-3 w-3" />
                    {record.checkOut}
                  </Badge>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      <p className="text-center text-xs text-muted-foreground">
        전체 출결 내역은 출결 관리 메뉴에서 확인하세요.
      </p>
    </div>
  );
}
