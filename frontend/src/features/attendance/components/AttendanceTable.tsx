import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { AttendanceStudentCell } from './AttendanceStudentCell';
import { AttendanceStatusBadge } from './AttendanceStatusBadge';
import { AttendanceEmptyState } from './AttendanceEmptyState';
import { formatDateHeader, isToday } from '../utils/dateUtils';
import { cn } from '@/shared/utils';
import type { RangeStudentRow } from '../types';

interface AttendanceTableProps {
  students: RangeStudentRow[];
  dates: string[];
  onStudentClick?: (studentId: string) => void;
  onStatusClick?: (studentId: string, date: string) => void;
}

export function AttendanceTable({
  students,
  dates,
  onStudentClick,
  onStatusClick,
}: AttendanceTableProps) {
  if (students.length === 0) {
    return (
      <div className="relative rounded-md border bg-white">
        <AttendanceEmptyState />
      </div>
    );
  }

  return (
    <div className="relative rounded-md border overflow-x-auto">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="sticky left-0 z-20 bg-white min-w-[200px] shadow-[2px_0_4px_-2px_rgba(0,0,0,0.1)]">
              원생 정보
            </TableHead>
            {dates.map((date) => (
              <TableHead
                key={date}
                className={cn(
                  'text-center min-w-[100px]',
                  isToday(date) && 'bg-primary/10 text-primary font-semibold'
                )}
              >
                {formatDateHeader(date)}
                {isToday(date) && (
                  <span className="ml-1 text-xs">(오늘)</span>
                )}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {students.map((student) => (
            <TableRow key={student.id}>
              <TableCell className="sticky left-0 z-10 bg-white shadow-[2px_0_4px_-2px_rgba(0,0,0,0.1)]">
                <AttendanceStudentCell
                  id={student.id}
                  name={student.name}
                  photoUrl={student.photoUrl}
                  className={student.className}
                  onClick={onStudentClick ? () => onStudentClick(student.id) : undefined}
                />
              </TableCell>
              {dates.map((date) => {
                const attendance = student.attendances[date];
                return (
                  <TableCell
                    key={date}
                    className={cn(
                      'text-center',
                      isToday(date) && 'bg-primary/5'
                    )}
                  >
                    <AttendanceStatusBadge
                      status={attendance?.status ?? null}
                      size="sm"
                      onClick={
                        onStatusClick ? () => onStatusClick(student.id, date) : undefined
                      }
                    />
                  </TableCell>
                );
              })}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
