import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Textarea } from '@/shared/components/ui/textarea';
import { Label } from '@/shared/components/ui/label';
import { useAttendanceStatusUpdate } from '../hooks/useAttendanceMutations';
import { ATTENDANCE_STATUS_LABELS } from '../constants';
import { AttendanceStatusBadge } from './AttendanceStatusBadge';
import type { AttendanceStatus } from '../types';

interface AttendanceStatusEditDialogProps {
  attendance: {
    id: string;
    studentName: string;
    date: string;
    currentStatus: AttendanceStatus;
  } | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess?: () => void;
  dojangId: number | null;
}

const STATUS_OPTIONS: AttendanceStatus[] = ['PRESENT', 'ABSENT', 'SICK', 'EXCUSED'];

export function AttendanceStatusEditDialog({
  attendance,
  open,
  onOpenChange,
  onSuccess,
  dojangId,
}: AttendanceStatusEditDialogProps) {
  const [selectedStatus, setSelectedStatus] = useState<AttendanceStatus | null>(null);
  const [notes, setNotes] = useState('');

  const { mutate: updateStatus, isPending } = useAttendanceStatusUpdate(dojangId);

  const handleSubmit = () => {
    if (!attendance || !selectedStatus) {
      return;
    }

    updateStatus(
      {
        id: attendance.id,
        status: selectedStatus,
        notes: notes.trim() || undefined,
      },
      {
        onSuccess: () => {
          onOpenChange(false);
          setSelectedStatus(null);
          setNotes('');
          onSuccess?.();
        },
      }
    );
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!isPending) {
      onOpenChange(newOpen);
      if (!newOpen) {
        setSelectedStatus(null);
        setNotes('');
      }
    }
  };

  if (!attendance) {
    return null;
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>출석 상태 수정</DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* 원생 정보 */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-500">원생 이름</span>
              <span className="font-medium">{attendance.studentName}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-500">날짜</span>
              <span className="font-medium">{attendance.date}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-500">현재 상태</span>
              <AttendanceStatusBadge status={attendance.currentStatus} />
            </div>
          </div>

          {/* 구분선 */}
          <div className="border-t" />

          {/* 변경할 상태 선택 */}
          <div className="space-y-3">
            <Label className="text-sm font-medium">변경할 상태</Label>
            <div className="grid grid-cols-2 gap-3">
              {STATUS_OPTIONS.map((status) => (
                <label
                  key={status}
                  className={`
                    flex items-center gap-3 p-3 rounded-lg border-2 cursor-pointer
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
                  />
                  <span className="text-sm font-medium">
                    {ATTENDANCE_STATUS_LABELS[status]}
                  </span>
                </label>
              ))}
            </div>
          </div>

          {/* 변경 사유 */}
          <div className="space-y-2">
            <Label htmlFor="notes" className="text-sm font-medium">
              변경 사유 (선택)
            </Label>
            <Textarea
              id="notes"
              placeholder="변경 사유를 입력하세요"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
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
            disabled={!selectedStatus || isPending}
          >
            {isPending ? '저장 중...' : '저장'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
