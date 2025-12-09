import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import { STUDENT_STATUS_LABEL, STUDENT_STATUS_COLOR, type StudentStatus } from '@/types/student';

interface StatusBadgeProps {
  status: StudentStatus;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  if (status === 'WITHDRAWN') return null;

  return (
    <Badge
      variant="outline"
      className={cn('border-0 font-medium', STUDENT_STATUS_COLOR[status])}
    >
      {STUDENT_STATUS_LABEL[status]}
    </Badge>
  );
}
