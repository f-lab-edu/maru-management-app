import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';
import { Label } from '@/shared/components/ui/label';
import { useAttendanceCheck } from '../hooks/useAttendanceMutations';
import { ATTENDANCE_STATUS_LABELS } from '../constants';
import type { AttendanceStatus } from '../types';

interface AttendanceCheckDialogProps {
  student: {
    id: string;
    name: string;
  } | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess?: () => void;
  dojangId?: number | null;
}

const CHECK_STATUS_OPTIONS: AttendanceStatus[] = ['PRESENT', 'SICK', 'EXCUSED'];

/**
 * 오늘 날짜를 YYYY-MM-DD 형식으로 반환
 */
function getTodayString(): string {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * 현재 시각을 HH:MM 형식으로 반환
 */
function getCurrentTimeString(): string {
  const now = new Date();
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
}

/**
 * 날짜가 과거 30일 이내인지 확인
 */
function isWithin30Days(dateString: string): boolean {
  const selected = new Date(dateString);
  const today = new Date();
  const thirtyDaysAgo = new Date(today);
  thirtyDaysAgo.setDate(today.getDate() - 30);

  return selected >= thirtyDaysAgo && selected <= today;
}

export function AttendanceCheckDialog({
  student,
  open,
  onOpenChange,
  onSuccess,
  dojangId = null,
}: AttendanceCheckDialogProps) {
  const [selectedStatus, setSelectedStatus] = useState<AttendanceStatus>('PRESENT');
  const [date, setDate] = useState(getTodayString());
  const [time, setTime] = useState(getCurrentTimeString());
  const [memo, setMemo] = useState('');
  const [dateError, setDateError] = useState('');

  const { mutate: checkAttendance, isPending } = useAttendanceCheck(dojangId);

  useEffect(() => {
    if (open) {
      setSelectedStatus('PRESENT');
      setDate(getTodayString());
      setTime(getCurrentTimeString());
      setMemo('');
      setDateError('');
    }
  }, [open]);

  const handleDateChange = (newDate: string) => {
    setDate(newDate);

    if (!isWithin30Days(newDate)) {
      setDateError('과거 30일 이내의 날짜만 선택할 수 있습니다');
    } else {
      setDateError('');
    }
  };

  const handleSubmit = () => {
    if (!student) {
      return;
    }

    if (!isWithin30Days(date)) {
      setDateError('과거 30일 이내의 날짜만 선택할 수 있습니다');
      return;
    }

    const checkinAt = `${date}T${time}:00`;

    checkAttendance(
      {
        studentId: student.id,
        status: selectedStatus,
        date,
        checkinAt,
        memo: memo.trim() || undefined,
      },
      {
        onSuccess: () => {
          onOpenChange(false);
          onSuccess?.();
        },
      }
    );
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!isPending) {
      onOpenChange(newOpen);
    }
  };

  if (!student) {
    return null;
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>출석 체크</DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* 원생 이름 */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-500">원생 이름</span>
              <span className="font-medium">{student.name}</span>
            </div>
          </div>

          {/* 구분선 */}
          <div className="border-t" />

          {/* 상태 선택 */}
          <div className="space-y-3">
            <Label className="text-sm font-medium">출석 상태</Label>
            <div className="grid grid-cols-3 gap-3">
              {CHECK_STATUS_OPTIONS.map((status) => (
                <label
                  key={status}
                  className={`
                    flex items-center justify-center gap-2 p-3 rounded-lg border-2 cursor-pointer
                    transition-all hover:bg-gray-50
                    ${
                      selectedStatus === status
                        ? 'border-primary bg-primary/5'
                        : 'border-gray-200'
                    }
                  `}
                >
                  <input
                    type="radio"
                    name="status"
                    value={status}
                    checked={selectedStatus === status}
                    onChange={(e) => setSelectedStatus(e.target.value as AttendanceStatus)}
                    className="h-4 w-4 text-primary focus:ring-primary"
                    disabled={isPending}
                  />
                  <span className="text-sm font-medium">
                    {ATTENDANCE_STATUS_LABELS[status]}
                  </span>
                </label>
              ))}
            </div>
          </div>

          {/* 날짜 선택 */}
          <div className="space-y-2">
            <Label htmlFor="date" className="text-sm font-medium">
              출석 날짜
            </Label>
            <Input
              id="date"
              type="date"
              value={date}
              onChange={(e) => handleDateChange(e.target.value)}
              max={getTodayString()}
              disabled={isPending}
              className={dateError ? 'border-red-500' : ''}
            />
            {dateError && (
              <p className="text-sm text-red-500">{dateError}</p>
            )}
          </div>

          {/* 시간 입력 */}
          <div className="space-y-2">
            <Label htmlFor="time" className="text-sm font-medium">
              체크인 시각
            </Label>
            <Input
              id="time"
              type="time"
              value={time}
              onChange={(e) => setTime(e.target.value)}
              disabled={isPending}
            />
          </div>

          {/* 메모 입력 */}
          <div className="space-y-2">
            <Label htmlFor="memo" className="text-sm font-medium">
              메모 (선택)
            </Label>
            <Textarea
              id="memo"
              placeholder="메모를 입력하세요"
              value={memo}
              onChange={(e) => setMemo(e.target.value)}
              rows={3}
              disabled={isPending}
            />
          </div>
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => handleOpenChange(false)}
            disabled={isPending}
          >
            취소
          </Button>
          <Button
            type="button"
            onClick={handleSubmit}
            disabled={isPending || !!dateError}
          >
            {isPending ? '처리 중...' : '출석 체크'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
