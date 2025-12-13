import { useMemo } from 'react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { AttendanceStatusBadge } from './AttendanceStatusBadge';
import { formatDateHeader, generateDatesInRange } from '../utils/dateUtils';
import { ATTENDANCE_STATUS_LABELS } from '../constants';
import type { RangeStudentRow, AttendanceStatus } from '../types';

interface AttendanceDetailSheetProps {
  student: RangeStudentRow | null;
  startDate: string;
  endDate: string;
  isOpen: boolean;
  onClose: () => void;
}

interface AttendanceHistoryItem {
  date: string;
  status: AttendanceStatus | null;
  checkinAt: string | null;
}

function SheetSkeleton() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Skeleton className="h-16 w-16 rounded-full" />
        <div className="space-y-2">
          <Skeleton className="h-6 w-32" />
          <Skeleton className="h-4 w-24" />
        </div>
      </div>
      <div className="space-y-4">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-32 w-full" />
      </div>
    </div>
  );
}

export function AttendanceDetailSheet({
  student,
  startDate,
  endDate,
  isOpen,
  onClose,
}: AttendanceDetailSheetProps) {
  const attendanceHistory = useMemo((): AttendanceHistoryItem[] => {
    if (!student || !startDate || !endDate) return [];

    const dates = generateDatesInRange(startDate, endDate);
    return dates
      .map((date) => {
        const attendance = student.attendances[date];
        return {
          date,
          status: attendance?.status ?? null,
          checkinAt: attendance?.checkinAt ?? null,
        };
      })
      .reverse();
  }, [student, startDate, endDate]);

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
    <Sheet open={isOpen} onOpenChange={(open) => !open && onClose()} modal={false}>
      <SheetContent side="right" className="w-[400px] overflow-y-auto sm:w-[480px]">
        {!student ? (
          <SheetSkeleton />
        ) : (
          <>
            <SheetHeader className="space-y-1 pt-4">
              <div className="flex items-center gap-4">
                <Avatar className="h-14 w-14">
                  <AvatarImage src={student.photoUrl ?? undefined} alt={student.name} />
                  <AvatarFallback className="bg-primary/10 text-primary text-lg font-medium">
                    {student.name.slice(0, 2)}
                  </AvatarFallback>
                </Avatar>
                <div>
                  <SheetTitle className="text-xl">{student.name}</SheetTitle>
                  <p className="text-sm text-muted-foreground">{student.className}</p>
                </div>
              </div>
            </SheetHeader>

            <div className="mt-6 space-y-6">
              {/* 출석 통계 */}
              <div className="grid grid-cols-4 gap-2">
                <div className="text-center p-3 border rounded-lg bg-card">
                  <div className="text-xl font-bold">{stats.present}</div>
                  <div className="text-[10px] text-muted-foreground">{ATTENDANCE_STATUS_LABELS.PRESENT}</div>
                </div>
                <div className="text-center p-3 border rounded-lg bg-card">
                  <div className="text-xl font-bold">{stats.absent}</div>
                  <div className="text-[10px] text-muted-foreground">{ATTENDANCE_STATUS_LABELS.ABSENT}</div>
                </div>
                <div className="text-center p-3 border rounded-lg bg-card">
                  <div className="text-xl font-bold">{stats.sick}</div>
                  <div className="text-[10px] text-muted-foreground">{ATTENDANCE_STATUS_LABELS.SICK}</div>
                </div>
                <div className="text-center p-3 border rounded-lg bg-card">
                  <div className="text-xl font-bold">{attendanceRate}%</div>
                  <div className="text-[10px] text-muted-foreground">출석률</div>
                </div>
              </div>

              {/* 출석 이력 */}
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <h4 className="font-medium">출석 이력</h4>
                  <Badge variant="outline" className="text-xs">
                    {startDate} ~ {endDate}
                  </Badge>
                </div>

                <ScrollArea className="h-[400px] rounded-md border">
                  <div className="p-2 space-y-1">
                    {attendanceHistory.map((item) => (
                      <div
                        key={item.date}
                        className="flex items-center justify-between py-3 px-3 rounded-md hover:bg-muted/50"
                      >
                        <span className="text-sm font-medium min-w-[100px]">
                          {formatDateHeader(item.date)}
                        </span>
                        <div className="flex items-center gap-3">
                          {item.checkinAt && (
                            <span className="text-xs text-muted-foreground">
                              {formatTime(item.checkinAt)}
                            </span>
                          )}
                          <AttendanceStatusBadge status={item.status} />
                        </div>
                      </div>
                    ))}
                  </div>
                </ScrollArea>
              </div>
            </div>
          </>
        )}
      </SheetContent>
    </Sheet>
  );
}
