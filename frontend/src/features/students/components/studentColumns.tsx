import { ColumnDef } from '@tanstack/react-table';
import { ArrowUpDown } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { StudentNameCell } from './StudentNameCell';
import { AgeCell } from './AgeCell';
import { StudentStatusSelect } from './StudentStatusSelect';
import { StudentRowActions } from './StudentRowActions';
import type { StudentSummary, StudentStatus } from '@/types/student';

interface ColumnOptions {
  onEdit: (student: StudentSummary) => void;
  onDelete: (student: StudentSummary) => void;
  onStatusChange: (student: StudentSummary, newStatus: StudentStatus) => void;
}

export function createStudentColumns({ onEdit, onDelete, onStatusChange }: ColumnOptions): ColumnDef<StudentSummary>[] {
  return [
    {
      id: 'select',
      header: ({ table }) => (
        <Checkbox
          checked={
            table.getIsAllPageRowsSelected() ||
            (table.getIsSomePageRowsSelected() && 'indeterminate')
          }
          onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
          aria-label="전체 선택"
        />
      ),
      cell: ({ row }) => (
        <Checkbox
          checked={row.getIsSelected()}
          onCheckedChange={(value) => row.toggleSelected(!!value)}
          aria-label="행 선택"
          onClick={(e) => e.stopPropagation()}
        />
      ),
      enableSorting: false,
      enableHiding: false,
    },
    {
      accessorKey: 'name',
      header: ({ column }) => (
        <Button
          variant="ghost"
          onClick={() => column.toggleSorting(column.getIsSorted() === 'asc')}
          className="-ml-4"
        >
          이름
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
      cell: ({ row }) => <StudentNameCell student={row.original} />,
    },
    {
      accessorKey: 'birth',
      header: ({ column }) => (
        <Button
          variant="ghost"
          onClick={() => column.toggleSorting(column.getIsSorted() === 'asc')}
          className="-ml-4"
        >
          나이 / 생년월일
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
      cell: ({ row }) => <AgeCell birth={row.original.birth} />,
    },
    {
      id: 'contact',
      header: '연락처',
      cell: ({ row }) => {
        const student = row.original;
        if (student.phone) return student.phone;
        if (student.primaryGuardianPhone) {
          return `${student.primaryGuardianName || '보호자'} ${student.primaryGuardianPhone}`;
        }
        return '-';
      },
    },
    {
      accessorKey: 'status',
      header: '상태',
      cell: ({ row }) => (
        <div onClick={(e) => e.stopPropagation()}>
          <StudentStatusSelect
            status={row.original.status}
            onStatusChange={(newStatus) => onStatusChange(row.original, newStatus)}
          />
        </div>
      ),
    },
    {
      id: 'actions',
      cell: ({ row }) => (
        <div onClick={(e) => e.stopPropagation()}>
          <StudentRowActions
            student={row.original}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        </div>
      ),
    },
  ];
}
