import { useMemo } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { useStudentAttendance } from '../hooks/useStudentAttendance';
import { AttendanceStatusBadge } from './AttendanceStatusBadge';
import { formatDateHeader, generateDatesInRange } from '../utils/dateUtils';
import { ATTENDANCE_STATUS_LABELS } from '../constants';
import type { AttendanceStatus } from '../types';

interface StudentDetailDialogProps {
  studentId: string | null;
  dojangId: number | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

interface AttendanceHistoryItem {
  date: string;
  status: AttendanceStatus | null;
  checkinAt: string | null;
}

export function StudentDetailDialog({
  studentId,
  dojangId,
  open,
  onOpenChange,
}: StudentDetailDialogProps) {
  const { data, isLoading } = useStudentAttendance({
    dojangId,
    studentId,
  });

  const student = data?.student;

  // 출석 이력을 날짜별로 정렬하여 리스트로 변환
  const attendanceHistory = useMemo((): AttendanceHistoryItem[] => {
    if (!student || !data) return [];

    const dates = generateDatesInRange(data.startDate, data.endDate);
    return dates
      .map((date) => {
        const attendance = student.attendances[date];
        return {
          date,
          status: attendance?.status ?? null,
          checkinAt: attendance?.checkinAt ?? null,
        };
      })
      .reverse(); // 최근 날짜가 위로
  }, [student, data]);

  // 출석 통계 계산
  const stats = useMemo(() => {
    if (!attendanceHistory.length) {
      return { present: 0, absent: 0, sick: 0, excused: 0, total: 0 };
    }

    const counts = { present: 0, absent: 0, sick: 0, excused: 0, total: 0 };

    attendanceHistory.forEach((item) => {
      if (item.status) {
        counts.total++;
        switch (item.status) {
          case 'PRESENT':
            counts.present++;
            break;
          case 'ABSENT':
            counts.absent++;
            break;
          case 'SICK':
            counts.sick++;
            break;
          case 'EXCUSED':
            counts.excused++;
            break;
        }
      }
    });

    return counts;
  }, [attendanceHistory]);

  const attendanceRate = stats.total > 0 ? Math.round((stats.present / stats.total) * 100) : 0;

  const formatTime = (isoString: string | null): string => {
    if (!isoString) return '-';
    const date = new Date(isoString);
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px] max-h-[90vh]">
        <DialogHeader>
          <DialogTitle>출석 상세 정보</DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className="py-8 text-center text-muted-foreground">
            불러오는 중...
          </div>
        ) : !student ? (
          <div className="py-8 text-center text-muted-foreground">
            학생 정보를 찾을 수 없습니다.
          </div>
        ) : (
          <div className="space-y-6">
            {/* 학생 정보 */}
            <div className="flex items-center gap-4">
              <Avatar className="h-16 w-16">
                <AvatarImage src={student.photoUrl ?? undefined} alt={student.name} />
                <AvatarFallback className="bg-primary/10 text-primary text-lg font-medium">
                  {student.name.slice(0, 2)}
                </AvatarFallback>
              </Avatar>
              <div>
                <h3 className="text-lg font-semibold">{student.name}</h3>
                <p className="text-sm text-muted-foreground">{student.className}</p>
              </div>
            </div>

            {/* 출석 통계 요약 */}
            <div className="grid grid-cols-4 gap-2">
              <div className="text-center p-3 bg-green-50 rounded-lg">
                <div className="text-lg font-bold text-green-600">{stats.present}</div>
                <div className="text-xs text-green-600">{ATTENDANCE_STATUS_LABELS.PRESENT}</div>
              </div>
              <div className="text-center p-3 bg-red-50 rounded-lg">
                <div className="text-lg font-bold text-red-600">{stats.absent}</div>
                <div className="text-xs text-red-600">{ATTENDANCE_STATUS_LABELS.ABSENT}</div>
              </div>
              <div className="text-center p-3 bg-orange-50 rounded-lg">
                <div className="text-lg font-bold text-orange-600">{stats.sick}</div>
                <div className="text-xs text-orange-600">{ATTENDANCE_STATUS_LABELS.SICK}</div>
              </div>
              <div className="text-center p-3 bg-blue-50 rounded-lg">
                <div className="text-lg font-bold text-blue-600">{attendanceRate}%</div>
                <div className="text-xs text-blue-600">출석률</div>
              </div>
            </div>

            {/* 출석 이력 */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-medium">출석 이력</h4>
                <Badge variant="outline" className="text-xs">
                  최근 {attendanceHistory.length}일
                </Badge>
              </div>

              <ScrollArea className="h-[240px] rounded-md border">
                <div className="p-2 space-y-1">
                  {attendanceHistory.map((item) => (
                    <div
                      key={item.date}
                      className="flex items-center justify-between py-2 px-3 rounded-md hover:bg-muted/50"
                    >
                      <span className="text-sm font-medium min-w-[90px]">
                        {formatDateHeader(item.date)}
                      </span>
                      <div className="flex items-center gap-3">
                        {item.checkinAt && (
                          <span className="text-xs text-muted-foreground">
                            {formatTime(item.checkinAt)}
                          </span>
                        )}
                        <AttendanceStatusBadge status={item.status} size="sm" />
                      </div>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
