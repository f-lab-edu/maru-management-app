import { MoreHorizontal, Pencil, Trash2 } from 'lucide-react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import { Button } from '@/shared/components/ui/button';
import type { StudentSummary } from '@/types/student';

interface StudentRowActionsProps {
  student: StudentSummary;
  onEdit: (student: StudentSummary) => void;
  onDelete: (student: StudentSummary) => void;
  canUpdate?: boolean;
  canDelete?: boolean;
}

export function StudentRowActions({ student, onEdit, onDelete, canUpdate = true, canDelete = true }: StudentRowActionsProps) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="h-8 w-8">
          <MoreHorizontal className="h-4 w-4" />
          <span className="sr-only">메뉴 열기</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={() => onEdit(student)} disabled={!canUpdate}>
          <Pencil className="mr-2 h-4 w-4" />
          수정
        </DropdownMenuItem>
        <DropdownMenuItem
          onClick={() => onDelete(student)}
          className="text-destructive focus:text-destructive"
          disabled={!canDelete}
        >
          <Trash2 className="mr-2 h-4 w-4" />
          삭제
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
