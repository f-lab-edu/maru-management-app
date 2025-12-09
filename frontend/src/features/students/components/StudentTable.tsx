import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { StudentNameCell } from './StudentNameCell';
import { AgeCell } from './AgeCell';
import { StatusBadge } from './StatusBadge';
import { StudentRowActions } from './StudentRowActions';
import type { StudentSummary } from '@/types/student';

interface StudentTableProps {
  students: StudentSummary[];
  isLoading: boolean;
  onRowClick: (studentId: number) => void;
  onEdit: (student: StudentSummary) => void;
  onDelete: (student: StudentSummary) => void;
}

function TableSkeleton() {
  return (
    <>
      {Array.from({ length: 5 }).map((_, i) => (
        <TableRow key={i}>
          <TableCell>
            <div className="flex items-center gap-3">
              <Skeleton className="h-9 w-9 rounded-full" />
              <Skeleton className="h-4 w-24" />
            </div>
          </TableCell>
          <TableCell>
            <Skeleton className="h-4 w-32" />
          </TableCell>
          <TableCell>
            <Skeleton className="h-4 w-28" />
          </TableCell>
          <TableCell>
            <Skeleton className="h-6 w-16 rounded-full" />
          </TableCell>
          <TableCell>
            <Skeleton className="h-8 w-8" />
          </TableCell>
        </TableRow>
      ))}
    </>
  );
}

function EmptyState() {
  return (
    <TableRow>
      <TableCell colSpan={5} className="h-32 text-center">
        <p className="text-muted-foreground">등록된 수련생이 없습니다.</p>
      </TableCell>
    </TableRow>
  );
}

export function StudentTable({
  students,
  isLoading,
  onRowClick,
  onEdit,
  onDelete,
}: StudentTableProps) {
  const getContactInfo = (student: StudentSummary) => {
    if (student.phone) return student.phone;
    if (student.primaryGuardianPhone) {
      return `${student.primaryGuardianName || '보호자'} ${student.primaryGuardianPhone}`;
    }
    return '-';
  };

  return (
    <div className="rounded-xl border bg-white shadow-sm">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[200px]">이름</TableHead>
            <TableHead className="w-[180px]">나이 / 생년월일</TableHead>
            <TableHead className="w-[180px]">연락처</TableHead>
            <TableHead className="w-[100px]">상태</TableHead>
            <TableHead className="w-[60px]" />
          </TableRow>
        </TableHeader>
        <TableBody>
          {isLoading ? (
            <TableSkeleton />
          ) : students.length === 0 ? (
            <EmptyState />
          ) : (
            students.map((student) => (
              <TableRow
                key={student.id}
                className="cursor-pointer hover:bg-muted/50"
                onClick={() => onRowClick(student.id)}
              >
                <TableCell>
                  <StudentNameCell student={student} />
                </TableCell>
                <TableCell>
                  <AgeCell birth={student.birth} />
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {getContactInfo(student)}
                </TableCell>
                <TableCell>
                  <StatusBadge status={student.status} />
                </TableCell>
                <TableCell onClick={(e) => e.stopPropagation()}>
                  <StudentRowActions
                    student={student}
                    onEdit={onEdit}
                    onDelete={onDelete}
                  />
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
}
