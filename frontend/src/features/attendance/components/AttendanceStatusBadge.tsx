import { Badge } from '@/shared/components/ui/badge';
import { ATTENDANCE_STATUS_LABELS, ATTENDANCE_STATUS_COLORS } from '../constants';
import type { AttendanceStatus } from '../types';

interface AttendanceStatusBadgeProps {
  status: AttendanceStatus | null;
  size?: 'sm' | 'md';
  onClick?: () => void;
}

const SIZE_CLASSES = {
  sm: 'px-1.5 py-0 text-[10px]',
  md: 'px-2 py-0.5 text-xs',
};

export function AttendanceStatusBadge({ status, size = 'md', onClick }: AttendanceStatusBadgeProps) {
  if (!status) {
    return <span className="text-muted-foreground text-xs">-</span>;
  }

  return (
    <Badge
      variant="secondary"
      className={`
        ${ATTENDANCE_STATUS_COLORS[status]}
        ${SIZE_CLASSES[size]}
        ${onClick ? 'cursor-pointer hover:opacity-80' : ''}
        font-medium
      `}
      onClick={onClick}
    >
      {ATTENDANCE_STATUS_LABELS[status]}
    </Badge>
  );
}
