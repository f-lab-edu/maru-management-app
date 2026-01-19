import { Avatar, AvatarFallback, AvatarImage } from '@/shared/components/ui/avatar';
import type { StudentSummary } from '@/types/student';

interface StudentNameCellProps {
  student: StudentSummary;
}

export function StudentNameCell({ student }: StudentNameCellProps) {
  return (
    <div className="flex items-center gap-3">
      <Avatar className="h-9 w-9">
        <AvatarImage src={student.photoUrl} alt={student.name} />
        <AvatarFallback className="bg-primary/10 text-primary text-sm font-medium">
          {student.name.slice(0, 2)}
        </AvatarFallback>
      </Avatar>
      <span className="font-medium">{student.name}</span>
    </div>
  );
}
