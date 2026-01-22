import { useMemo } from 'react';
import { format, startOfMonth } from 'date-fns';
import { Calendar, TrendingUp } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { useAuthStore } from '@/stores/authStore';
import { useStudentAttendance } from '@/features/attendance/hooks/useStudentAttendance';
import { ATTENDANCE_STATUS_LABELS, ATTENDANCE_STATUS_COLORS } from '@/features/attendance/constants';
import type { AttendanceStatus } from '@/features/attendance/types';
import { cn } from '@/shared/utils';

interface AttendanceHistoryTabProps {
  studentId: string;
}

export function AttendanceHistoryTab({ studentId }: AttendanceHistoryTabProps) {
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? null;

  const today = new Date();
  const startDate = format(startOfMonth(today), 'yyyy-MM-dd');
  const endDate = format(today, 'yyyy-MM-dd');

  const { data: attendanceList, isLoading } = useStudentAttendance({
    dojangId,
    studentId,
    startDate,
    endDate,
  });

  const stats = useMemo(() => {
    if (!attendanceList || attendanceList.length === 0) {
      return { monthlyRate: 0, totalDays: 0, attendedDays: 0 };
    }

    const totalDays = attendanceList.length;
    const attendedDays = attendanceList.filter((r) => r.status === 'PRESENT').length;
    const monthlyRate = totalDays > 0 ? Math.round((attendedDays / totalDays) * 100) : 0;

    return { monthlyRate, totalDays, attendedDays };
  }, [attendanceList]);

  const recentAttendance = useMemo(() => {
    if (!attendanceList) return [];
    return [...attendanceList]
      .sort((a, b) => new Date(b.attendanceDate).getTime() - new Date(a.attendanceDate).getTime())
      .slice(0, 5);
  }, [attendanceList]);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
      weekday: 'short',
    });
  };

  const formatTime = (isoString: string | null): string | null => {
    if (!isoString) return null;
    const date = new Date(isoString);
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
  };

  const getStatusBadge = (status: AttendanceStatus) => {
    const colorClass = ATTENDANCE_STATUS_COLORS[status];
    const label = ATTENDANCE_STATUS_LABELS[status];
    return (
      <Badge variant="outline" className={cn('text-[10px] px-1.5 py-0.5', colorClass)}>
        {label}
      </Badge>
    );
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-20 w-full" />
        <div className="space-y-2">
          {[1, 2, 3, 4, 5].map((i) => (
            <Skeleton key={i} className="h-14 w-full" />
          ))}
        </div>
      </div>
    );
  }

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

        {recentAttendance.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <Calendar className="h-8 w-8 mx-auto mb-2 opacity-50" />
            <p>출결 기록이 없습니다</p>
          </div>
        ) : (
          <div className="space-y-1">
            {recentAttendance.map((record) => {
              const showTime = record.status === 'PRESENT';
              return (
                <div
                  key={record.id}
                  className="flex items-center justify-between rounded-lg border p-3"
                >
                  <span className="text-sm font-medium">{formatDate(record.attendanceDate)}</span>
                  <div className="flex items-center gap-2">
                    {showTime && record.checkinAt && (
                      <span className="text-[11px] text-blue-600 font-medium">
                        {formatTime(record.checkinAt)}
                      </span>
                    )}
                    {showTime && record.checkinAt && record.checkoutAt && (
                      <span className="text-muted-foreground">→</span>
                    )}
                    {showTime && record.checkoutAt && (
                      <span className="text-[11px] text-gray-500 font-medium">
                        {formatTime(record.checkoutAt)}
                      </span>
                    )}
                    {!showTime && record.status !== 'PRESENT' && (
                      <span className="text-[11px] text-muted-foreground">-</span>
                    )}
                    {getStatusBadge(record.status)}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <p className="text-center text-xs text-muted-foreground">
        전체 출결 내역은 출결 관리 메뉴에서 확인하세요.
      </p>
    </div>
  );
}
