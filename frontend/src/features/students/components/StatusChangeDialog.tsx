import { useState } from 'react';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui/alert-dialog';
import { Textarea } from '@/shared/components/ui/textarea';
import { Label } from '@/shared/components/ui/label';
import { STUDENT_STATUS_LABEL, type StudentStatus } from '@/types/student';

interface StatusChangeDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  studentName: string;
  currentStatus: StudentStatus;
  newStatus: StudentStatus;
  onConfirm: (reason?: string) => void;
  isLoading?: boolean;
}

export function StatusChangeDialog({
  open,
  onOpenChange,
  studentName,
  currentStatus,
  newStatus,
  onConfirm,
  isLoading,
}: StatusChangeDialogProps) {
  const [reason, setReason] = useState('');

  const handleConfirm = () => {
    onConfirm(reason.trim() || undefined);
    setReason('');
  };

  const handleCancel = () => {
    onOpenChange(false);
    setReason('');
  };

  const currentStatusLabel = currentStatus !== 'WITHDRAWN' ? STUDENT_STATUS_LABEL[currentStatus] : '탈퇴';
  const newStatusLabel = newStatus !== 'WITHDRAWN' ? STUDENT_STATUS_LABEL[newStatus] : '탈퇴';

  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>수련생 상태 변경</AlertDialogTitle>
          <AlertDialogDescription asChild>
            <div className="space-y-4">
              <p>
                <span className="font-medium text-foreground">{studentName}</span> 수련생의 상태를{' '}
                <span className="font-medium text-foreground">{currentStatusLabel}</span>에서{' '}
                <span className="font-medium text-foreground">{newStatusLabel}</span>(으)로 변경하시겠습니까?
              </p>
              <div className="space-y-2">
                <Label htmlFor="reason">변경 사유 (선택)</Label>
                <Textarea
                  id="reason"
                  placeholder="상태 변경 사유를 입력하세요"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  rows={3}
                  disabled={isLoading}
                />
              </div>
            </div>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel onClick={handleCancel} disabled={isLoading}>
            취소
          </AlertDialogCancel>
          <AlertDialogAction onClick={handleConfirm} disabled={isLoading}>
            {isLoading ? '변경 중...' : '변경'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
