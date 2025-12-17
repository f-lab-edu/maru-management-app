import { Badge } from '@/shared/components/ui/badge';
import {
  ATTENDANCE_STATUS_LABELS,
  ATTENDANCE_STATUS_COLORS,
  ATTENDANCE_DISPLAY_LABELS,
  IN_CLASS_COLOR,
  CHECKED_OUT_COLOR,
} from '../constants';
import type { AttendanceStatus } from '../types';
import { isToday } from '../utils/dateUtils';

interface AttendanceStatusBadgeProps {
  status: AttendanceStatus | null;
  checkoutAt?: string | null;
  date?: string;
  size?: 'sm' | 'md';
  onClick?: () => void;
}

const SIZE_CLASSES = {
  sm: 'px-1.5 py-0 text-[10px]',
  md: 'px-2 py-0.5 text-xs',
};

export function AttendanceStatusBadge({ status, checkoutAt, date, size = 'md', onClick }: AttendanceStatusBadgeProps) {
  if (!status) {
    return <span className="text-muted-foreground text-xs">-</span>;
  }

  const getDisplayInfo = () => {
    if (status === 'PRESENT') {
      // 과거 날짜는 '출석'으로 표시, 오늘만 '수업중'/'하원' 구분
      const isTodayDate = date ? isToday(date) : true;
      if (!isTodayDate) {
        return { label: ATTENDANCE_STATUS_LABELS.PRESENT, color: ATTENDANCE_STATUS_COLORS.PRESENT };
      }
      if (checkoutAt) {
        return { label: ATTENDANCE_DISPLAY_LABELS.CHECKED_OUT, color: CHECKED_OUT_COLOR };
      }
      return { label: ATTENDANCE_DISPLAY_LABELS.IN_CLASS, color: IN_CLASS_COLOR };
    }
    return { label: ATTENDANCE_STATUS_LABELS[status], color: ATTENDANCE_STATUS_COLORS[status] };
  };

  const { label, color } = getDisplayInfo();

  return (
    <Badge
      variant="secondary"
      className={`
        ${color}
        ${SIZE_CLASSES[size]}
        ${onClick ? 'cursor-pointer hover:opacity-80' : ''}
        font-medium
      `}
      onClick={onClick}
    >
      {label}
    </Badge>
  );
}
