import { ColumnDef } from '@tanstack/react-table';
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/components/ui/avatar';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/shared/components/ui/tooltip';
import { AttendanceStatusBadge } from './AttendanceStatusBadge';
import type { StudentAttendanceRow, ViewMode, AttendanceStatus } from '../types';
import { ATTENDANCE_STATUS_LABELS } from '../constants';
import { isToday } from '../utils/dateUtils';

interface ColumnOptions {
  dates: string[];
  viewMode: ViewMode;
}

const STATUS_DOT_COLORS: Record<AttendanceStatus, string> = {
  PRESENT: 'bg-emerald-600',
  ABSENT: 'bg-red-500',
  SICK: 'bg-amber-500',
  EXCUSED: 'bg-blue-500',
};

const DAYS = ['일', '월', '화', '수', '목', '금', '토'];

function formatDayHeader(dateStr: string, viewMode: ViewMode): { day: string; date: string } {
  const dateObj = new Date(dateStr);
  const dayName = DAYS[dateObj.getDay()];
  const dateNum = String(dateObj.getDate());

  if (viewMode === 'weekly') {
    return { day: dayName, date: `${dateObj.getMonth() + 1}/${dateNum}` };
  }
  return { day: dateNum, date: dayName };
}

function StatusDot({ status }: { status: AttendanceStatus | null }) {
  if (!status) {
    return <div className="w-2.5 h-2.5 rounded-full bg-muted" />;
  }
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div className={`w-2.5 h-2.5 rounded-full cursor-pointer ${STATUS_DOT_COLORS[status]}`} />
      </TooltipTrigger>
      <TooltipContent side="top" className="text-xs">
        {ATTENDANCE_STATUS_LABELS[status]}
      </TooltipContent>
    </Tooltip>
  );
}

export function createAttendanceColumns({ dates, viewMode }: ColumnOptions): ColumnDef<StudentAttendanceRow>[] {
  const dateColumns: ColumnDef<StudentAttendanceRow>[] = dates.map((date) => {
    const { day, date: subText } = formatDayHeader(date, viewMode);
    const isTodayDate = isToday(date);

    return {
      id: `date-${date}`,
      header: () => (
        <div className="flex flex-col items-center leading-tight">
          <span className={`text-xs font-medium ${isTodayDate ? 'text-primary' : ''}`}>{day}</span>
          <span className={`text-[10px] ${isTodayDate ? 'text-primary' : 'text-muted-foreground'}`}>{subText}</span>
        </div>
      ),
      cell: ({ row }) => {
        const attendance = row.original.attendances[date];
        const status = attendance?.status ?? null;
        const checkoutAt = attendance?.checkoutAt ?? null;

        if (viewMode === 'monthly') {
          return (
            <div className="flex justify-center">
              <StatusDot status={status} />
            </div>
          );
        }

        return (
          <div className="flex justify-center">
            <AttendanceStatusBadge status={status} checkoutAt={checkoutAt} date={date} size="sm" />
          </div>
        );
      },
      size: viewMode === 'monthly' ? 36 : 52,
      minSize: viewMode === 'monthly' ? 36 : 52,
      maxSize: viewMode === 'monthly' ? 36 : 52,
    };
  });

  return [
    {
      id: 'student',
      header: '이름',
      cell: ({ row }) => {
        const student = row.original;
        return (
          <div
            className="flex items-center gap-2 cursor-pointer -m-4 p-4"
            onClick={(e) => {
              e.stopPropagation();
              row.toggleSelected();
            }}
          >
            <Avatar className="h-7 w-7">
              <AvatarImage src={student.photoUrl ?? undefined} alt={student.name} />
              <AvatarFallback className="bg-primary/10 text-primary text-xs font-medium">
                {student.name.slice(0, 2)}
              </AvatarFallback>
            </Avatar>
            <span className="text-sm font-medium">{student.name}</span>
          </div>
        );
      },
      size: 140,
      minSize: 140,
      maxSize: 140,
    },
    ...dateColumns,
    {
      id: 'attendanceRate',
      header: '출석률',
      cell: ({ row }) => {
        const student = row.original;
        let present = 0;
        let total = 0;

        dates.forEach((date) => {
          const att = student.attendances[date];
          if (att) {
            total++;
            if (att.status === 'PRESENT') present++;
          }
        });

        const rate = total > 0 ? Math.round((present / total) * 100) : 0;
        return (
          <div className="flex items-center gap-2">
            <div className="w-16 h-1.5 bg-muted rounded-full overflow-hidden">
              <div
                className="h-full bg-foreground rounded-full transition-all"
                style={{ width: `${rate}%` }}
              />
            </div>
            <span className="text-xs font-medium w-8">{rate}%</span>
          </div>
        );
      },
      size: 110,
      minSize: 110,
      maxSize: 110,
    },
    {
      id: 'summary',
      header: '상세',
      cell: ({ row }) => {
        const student = row.original;
        let present = 0;
        let absent = 0;
        let sick = 0;
        let excused = 0;

        dates.forEach((date) => {
          const att = student.attendances[date];
          if (att) {
            switch (att.status) {
              case 'PRESENT':
                present++;
                break;
              case 'ABSENT':
                absent++;
                break;
              case 'SICK':
                sick++;
                break;
              case 'EXCUSED':
                excused++;
                break;
            }
          }
        });

        return (
          <div className="flex items-center gap-2 text-xs">
            <span className="font-medium">{present}출석</span>
            <span className="text-muted-foreground">{absent}결석</span>
            {sick > 0 && <span className="text-muted-foreground">{sick}병결</span>}
            {excused > 0 && <span className="text-muted-foreground">{excused}공결</span>}
          </div>
        );
      },
      size: 140,
      minSize: 140,
      maxSize: 140,
    },
  ];
}
