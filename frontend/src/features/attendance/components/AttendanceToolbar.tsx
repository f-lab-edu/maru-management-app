import { Search } from 'lucide-react';
import type { DateRange } from 'react-day-picker';
import { Input } from '@/shared/components/ui/input';
import {
  ToggleGroup,
  ToggleGroupItem,
} from '@/shared/components/ui/toggle-group';
import { DateRangePicker } from '@/shared/components/ui/date-range-picker';
import { cn } from '@/shared/utils';
import type { AttendanceStatus, ViewMode } from '../types';
import { ATTENDANCE_STATUS_LABELS, VIEW_MODE_OPTIONS } from '../constants';

interface AttendanceToolbarProps {
  searchQuery: string;
  onSearchQueryChange: (query: string) => void;
  statusFilter: AttendanceStatus | 'ALL';
  onStatusFilterChange: (status: AttendanceStatus | 'ALL') => void;
  viewMode: ViewMode;
  onViewModeChange: (mode: ViewMode) => void;
  dateRange: DateRange | undefined;
  onDateRangeChange: (range: DateRange | undefined) => void;
}

export const AttendanceToolbar = ({
  searchQuery,
  onSearchQueryChange,
  statusFilter,
  onStatusFilterChange,
  viewMode,
  onViewModeChange,
  dateRange,
  onDateRangeChange,
}: AttendanceToolbarProps) => {
  const statusOptions: { value: AttendanceStatus | 'ALL'; label: string }[] = [
    { value: 'ALL', label: '전체' },
    { value: 'PRESENT', label: ATTENDANCE_STATUS_LABELS.PRESENT },
    { value: 'ABSENT', label: ATTENDANCE_STATUS_LABELS.ABSENT },
    { value: 'SICK', label: ATTENDANCE_STATUS_LABELS.SICK },
    { value: 'EXCUSED', label: ATTENDANCE_STATUS_LABELS.EXCUSED },
  ];

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        {/* 좌측: 검색 Input */}
        <div className="relative w-full md:w-80">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            type="text"
            placeholder="원생 이름으로 검색..."
            value={searchQuery}
            onChange={(e) => onSearchQueryChange(e.target.value)}
            className="pl-9"
          />
        </div>

        {/* 우측: View 토글 및 DateRangePicker */}
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
          {/* View 토글 (주간/월간) */}
          <ToggleGroup
            type="single"
            value={viewMode}
            onValueChange={(value) => {
              if (value) onViewModeChange(value as ViewMode);
            }}
            className="justify-start"
          >
            {VIEW_MODE_OPTIONS.map((option) => (
              <ToggleGroupItem
                key={option.value}
                value={option.value}
                aria-label={option.label}
                className="min-w-[60px]"
              >
                {option.label}
              </ToggleGroupItem>
            ))}
          </ToggleGroup>

          {/* DateRangePicker */}
          <DateRangePicker
            dateRange={dateRange}
            onDateRangeChange={onDateRangeChange}
            placeholder="기간을 선택하세요"
            className="w-full sm:w-auto"
          />
        </div>
      </div>

      {/* 중앙: 필터 Badge/Chip (전체, 출석, 결석, 병결, 공결) */}
      <div className="flex flex-wrap gap-2">
        <ToggleGroup
          type="single"
          value={statusFilter}
          onValueChange={(value) => {
            if (value) onStatusFilterChange(value as AttendanceStatus | 'ALL');
          }}
          className="justify-start"
        >
          {statusOptions.map((option) => (
            <ToggleGroupItem
              key={option.value}
              value={option.value}
              aria-label={option.label}
              className={cn(
                'min-w-[60px]',
                statusFilter === option.value &&
                  'bg-primary text-primary-foreground hover:bg-primary/90'
              )}
            >
              {option.label}
            </ToggleGroupItem>
          ))}
        </ToggleGroup>
      </div>
    </div>
  );
};
