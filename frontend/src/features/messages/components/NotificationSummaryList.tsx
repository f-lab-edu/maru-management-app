import { useState } from 'react';
import { Bell, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, CalendarIcon } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { Calendar } from '@/shared/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/shared/components/ui/popover';
import { ToggleGroup, ToggleGroupItem } from '@/shared/components/ui/toggle-group';
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from '@/shared/components/ui/table';
import { cn } from '@/shared/utils';
import { useAuthStore } from '@/stores/authStore';
import { useNotificationTimeline } from '../hooks/useNotifications';

const getTodayString = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const toLocalDateString = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

const formatDateNavLabel = (dateStr: string) => {
  const today = getTodayString();
  const date = new Date(dateStr + 'T00:00:00');
  const label = date.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
  return dateStr === today ? `${label} (오늘)` : label;
};

const addDays = (dateStr: string, days: number) => {
  const d = new Date(dateStr + 'T00:00:00');
  d.setDate(d.getDate() + days);
  return toLocalDateString(d);
};

const parseLocalDate = (dateStr: string) => new Date(dateStr + 'T00:00:00');

const MESSAGE_TYPE_TAG: Record<string, { label: string; className: string }> = {
  ATTENDANCE_CHECKIN: { label: '입관', className: 'bg-emerald-100 text-emerald-700 border-emerald-200' },
  ATTENDANCE_CHECKOUT: { label: '퇴관', className: 'bg-amber-100 text-amber-700 border-amber-200' },
  PAYMENT: { label: '결제', className: 'bg-purple-100 text-purple-700 border-purple-200' },
};

const TYPE_FILTER_OPTIONS = [
  { value: '', label: '전체' },
  { value: 'ATTENDANCE_CHECKIN', label: '입관' },
  { value: 'ATTENDANCE_CHECKOUT', label: '퇴관' },
  { value: 'PAYMENT', label: '결제' },
];

const STATUS_CONFIG: Record<string, { label: string; className: string }> = {
  ACCEPTED: { label: '성공', className: 'text-green-600' },
  DEAD: { label: '실패', className: 'text-red-600' },
};

const formatTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
};

export const NotificationSummaryList = () => {
  const [selectedDate, setSelectedDate] = useState(getTodayString);
  const [selectedType, setSelectedType] = useState('');
  const [page, setPage] = useState(0);
  const [calendarOpen, setCalendarOpen] = useState(false);
  const { selectedDojang } = useAuthStore();
  const dojangId = selectedDojang?.dojangId ?? '';

  const { data: timelineData, isLoading } = useNotificationTimeline(
    dojangId, selectedDate, selectedType || undefined, page,
  );

  const handlePrevDate = () => {
    setSelectedDate((d) => addDays(d, -1));
    setPage(0);
  };

  const handleNextDate = () => {
    setSelectedDate((d) => addDays(d, 1));
    setPage(0);
  };

  const handleCalendarSelect = (date: Date | undefined) => {
    if (date) {
      setSelectedDate(toLocalDateString(date));
      setPage(0);
    }
    setCalendarOpen(false);
  };

  const handleTypeChange = (value: string) => {
    setSelectedType(value);
    setPage(0);
  };

  return (
    <div className="space-y-4">
      {/* 날짜 네비게이션 */}
      <div className="flex items-center justify-center gap-3">
        <Button variant="outline" size="icon" className="h-8 w-8" onClick={handlePrevDate}>
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <Popover open={calendarOpen} onOpenChange={setCalendarOpen}>
          <PopoverTrigger asChild>
            <Button variant="ghost" className="text-sm font-medium min-w-[160px] gap-1.5">
              <CalendarIcon className="h-3.5 w-3.5" />
              {formatDateNavLabel(selectedDate)}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-auto p-0" align="center">
            <Calendar
              mode="single"
              selected={parseLocalDate(selectedDate)}
              onSelect={handleCalendarSelect}
              defaultMonth={parseLocalDate(selectedDate)}
            />
          </PopoverContent>
        </Popover>
        <Button variant="outline" size="icon" className="h-8 w-8" onClick={handleNextDate}>
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>

      {/* 유형 칩 필터 */}
      <ToggleGroup
        type="single"
        value={selectedType}
        onValueChange={handleTypeChange}
        className="justify-start"
      >
        {TYPE_FILTER_OPTIONS.map((opt) => (
          <ToggleGroupItem
            key={opt.value}
            value={opt.value}
            className="text-xs px-3 py-1 h-7 data-[state=on]:bg-foreground data-[state=on]:text-background data-[state=on]:font-semibold"
          >
            {opt.label}
          </ToggleGroupItem>
        ))}
      </ToggleGroup>

      {/* 테이블 */}
      <div className="rounded-xl border bg-white shadow-sm">
        <Table>
          <TableHeader className="bg-muted">
            <TableRow>
              <TableHead className="w-[70px]">유형</TableHead>
              <TableHead>원생</TableHead>
              <TableHead className="w-[60px]">상태</TableHead>
              <TableHead className="w-[100px]">시간</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableSkeleton />
            ) : !timelineData || timelineData.content.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="h-32 text-center">
                  <div className="flex flex-col items-center text-muted-foreground">
                    <Bell className="h-8 w-8 mb-2 opacity-50" />
                    <p className="text-sm">자동 알림 내역이 없습니다</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              timelineData.content.map((item) => {
                const tag = MESSAGE_TYPE_TAG[item.messageType];
                const status = STATUS_CONFIG[item.status];
                return (
                  <TableRow key={item.id}>
                    <TableCell>
                      {tag ? (
                        <Badge variant="outline" className={cn('text-[11px] px-1.5 py-0 font-medium', tag.className)}>
                          {tag.label}
                        </Badge>
                      ) : (
                        <Badge variant="outline" className="text-[11px] px-1.5 py-0 font-medium">
                          {item.messageTypeLabel}
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell className="font-medium">{item.studentName ?? '-'}</TableCell>
                    <TableCell>
                      {status ? (
                        <span className={cn('text-sm font-medium', status.className)}>{status.label}</span>
                      ) : (
                        <span className="text-sm text-muted-foreground">{item.status}</span>
                      )}
                    </TableCell>
                    <TableCell className="text-muted-foreground whitespace-nowrap">{formatTime(item.createdAt)}</TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>

        {/* 페이징 */}
        {timelineData && timelineData.totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 border-t px-4 py-3">
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8"
              onClick={() => setPage(0)}
              disabled={page === 0}
            >
              <ChevronsLeft className="h-4 w-4" />
            </Button>
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8"
              onClick={() => setPage((p) => p - 1)}
              disabled={page === 0}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <span className="text-sm text-muted-foreground px-2">
              {page + 1} / {timelineData.totalPages}
            </span>
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8"
              onClick={() => setPage((p) => p + 1)}
              disabled={page >= timelineData.totalPages - 1}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8"
              onClick={() => setPage(timelineData.totalPages - 1)}
              disabled={page >= timelineData.totalPages - 1}
            >
              <ChevronsRight className="h-4 w-4" />
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};

const TableSkeleton = () => (
  <>
    {Array.from({ length: 5 }).map((_, i) => (
      <TableRow key={i}>
        {Array.from({ length: 4 }).map((_, j) => (
          <TableCell key={j}>
            <Skeleton className="h-4 w-full" />
          </TableCell>
        ))}
      </TableRow>
    ))}
  </>
);
